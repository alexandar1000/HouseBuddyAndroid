package allurosi.housebuddy.shoppingList;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
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

    public interface AddShoppingItemDialogueListener {
        void onFinishAddDialog(ShoppingItem shoppingItem);
        void onFinishEditDialog(ShoppingItem shoppingItem, int position);
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_shopping_list_item, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditName = (EditText) view.findViewById(R.id.itemNameEdit);
        mEditInfo = (EditText) view.findViewById(R.id.itemInfoEdit);
        mAddSItemBtn = (Button) view.findViewById(R.id.addShoppingItemBtn);
        mCancelAddSItemBtn = (Button) view.findViewById(R.id.cancelAddingShoppingItemBtn);
        // Fetch arguments from bundle and set title

        if (getArguments().size() > 1) {
            mEditName.setText(getArguments().getString("name"));
            mEditInfo.setText(getArguments().getString("info"));
        }

        mCancelAddSItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mAddSItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddShoppingItemDialogueListener listener = (AddShoppingItemDialogueListener) getActivity();
                ShoppingItem shoppingItem = new ShoppingItem(mEditName.getText().toString(), mEditInfo.getText().toString());
                if (getArguments().size() > 1) {
                    listener.onFinishEditDialog(shoppingItem, Integer.parseInt(getArguments().getString("position")));
                } else {
                    listener.onFinishAddDialog(shoppingItem);
                }
                dismiss();
            }
        });

        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
    }
}
