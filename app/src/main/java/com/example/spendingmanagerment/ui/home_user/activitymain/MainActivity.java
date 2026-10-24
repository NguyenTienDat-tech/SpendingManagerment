package com.example.spendingmanagerment.ui.home_user.activitymain;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.ViewModelProvider;
import com.example.spendingmanagerment.R;
import com.example.spendingmanagerment.data.repository.ExcelRepository;
import com.example.spendingmanagerment.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private MainViewModel viewModel;
    private PopupMenu moreMenu;
    private final ActivityResultLauncher<String> exportExcelLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ExcelRepository.MIME_TYPE), uri -> viewModel.exportExcel(uri));
    private final ActivityResultLauncher<String[]> importExcelLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> viewModel.onImportFileSelected(uri));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), SystemBarStyle.dark(android.graphics.Color.TRANSPARENT));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });


        setupController();
        setupBottomNav();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        stateData();
        eventData();
    }

    private void setupController() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHomeFragment);
        if (host == null) throw new IllegalStateException("Missing NavHostFragment");
        navController = host.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() != R.id.addTransactionFragment) binding.toolBar.setTitle(destination.getLabel());
        });
    }

    private void setupBottomNav() {
        binding.bottomNav.setItemIconSize(getResources().getDimensionPixelSize(R.dimen.bottom_nav_icon_size));
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.more) {
                showMoreMenu();
                return false;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    private void showMoreMenu() {
        View anchor = binding.bottomNav.findViewById(R.id.more);
        moreMenu = new PopupMenu(this, anchor == null ? binding.bottomNav : anchor);
        moreMenu.inflate(R.menu.more_menu);
        moreMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.export_excel) viewModel.exportExcelClick();
            else if (item.getItemId() == R.id.import_excel) viewModel.importExcelClick();
            else if (item.getItemId() == R.id.clear_data) viewModel.clearDataClick();
            else return false;
            return true;
        });
        moreMenu.show();
    }

    private void stateData() {
        viewModel.getState().observe(this, state -> binding.bottomNav.getMenu().findItem(R.id.more).setEnabled(!state.busy));
    }

    private void eventData() {
        viewModel.getEvent().observe(this, event -> {
            MainEvent effect = event.consume();
            if (effect == null) return;
            switch (effect.type) {
                case EXPORT_PICKER:
                    exportExcelLauncher.launch("Transactions.xlsx");
                    break;
                case IMPORT_PICKER:
                    importExcelLauncher.launch(new String[] {ExcelRepository.MIME_TYPE});
                    break;
                case CONFIRM_IMPORT:
                    new MaterialAlertDialogBuilder(this).setMessage(R.string.confirm_import_excel)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.import_action, (dialog, which) -> viewModel.importExcel(effect.uri)).show();
                    break;
                case CONFIRM_CLEAR:
                    new MaterialAlertDialogBuilder(this).setMessage(R.string.confirm_clear_data)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.app_delete, (dialog, which) -> viewModel.clearData()).show();
                    break;
                case MESSAGE:
                    Toast.makeText(this, getString(effect.messageRes, effect.count), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override protected void onDestroy() {
        if (moreMenu != null) moreMenu.dismiss();
        super.onDestroy();
    }
}
