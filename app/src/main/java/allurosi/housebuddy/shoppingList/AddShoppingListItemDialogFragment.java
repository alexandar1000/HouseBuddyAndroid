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
import android.widget.EditText;

import allurosi.housebuddy.R;

public class AddShoppingListItemDialogFragment extends DialogFragment {

    private EditText mEditName;
    private EditText mEditInfo;

    private AddShoppingListItemListener listener;

    public interface AddShoppingListItemListener {
        void onFinishAddShoppingListItem(ShoppingItem shoppingItem);
        void onCloseNewShoppingItemDialog();
    }

    public AddShoppingListItemDialogFragment() {}

    public static AddShoppingListItemDialogFragment newInstance(String title) {
        AddShoppingListItemDialogFragment fragment = new AddShoppingListItemDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddShoppingListItemDialogFragment newInstance(String title, String name, String info, int position) {
        AddShoppingListItemDialogFragment fragment = new AddShoppingListItemDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("name", name);
        args.putString("info", info);
        args.putString("position", Integer.toString(position));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmet_new_shopping_list_item, container, false);

        // Get field from view
        mEditName = (EditText) view.findViewById(R.id.itemNameEdit);
        mEditInfo = (EditText) view.findViewById(R.id.itemInfoEdit);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditName = (EditText) view.findViewById(R.id.itemNameEdit);
        mEditInfo = (EditText) view.findViewById(R.id.itemInfoEdit);


        // Fetch arguments from bundle and set title
        if (getArguments() != null && getArguments().size() > 1) {
            mEditName.setText(getArguments().getString("name"));
            mEditInfo.setText(getArguments().getString("info"));
        }

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
                listener.onFinishAddShoppingListItem(shoppingItem);
                dismiss();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setListener(AddShoppingListItemListener parent) {
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
