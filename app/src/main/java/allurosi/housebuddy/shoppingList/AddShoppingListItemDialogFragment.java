package allurosi.housebuddy.shoppingList;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import allurosi.housebuddy.R;

public class AddShoppingListItemDialogFragment extends DialogFragment {

    private EditText mEditName;
    private EditText mEditInfo;
    private Button mAddSItemBtn;
    private Button mCancelAddSItemBtn;

    private AddShoppingItemDialogListener listener;

    public interface AddShoppingItemDialogListener {
        void onFinishEditDialog(ShoppingItem shoppingItem);

        void onCloseNewShoppingItemDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_shopping_list_item, container, false);

        // Get field from view
        mEditName = (EditText) view.findViewById(R.id.itemNameEdit);
        mEditInfo = (EditText) view.findViewById(R.id.itemInfoEdit);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                dismiss();
                return true;

            case R.id.action_save:
                ShoppingItem shoppingItem = new ShoppingItem(mEditName.getText().toString(), mEditInfo.getText().toString());
                listener.onFinishEditDialog(shoppingItem);
                dismiss();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListener(AddShoppingItemDialogListener parent) {
        listener = (ShoppingListFragment) parent;
    }

    @Override
    public void dismiss() {
        listener.onCloseNewShoppingItemDialog();
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
        super.dismiss();
    }

    public EditText getEditName() {
        return mEditName;
    }

    public EditText getEditInfo() {
        return mEditInfo;
    }
}
