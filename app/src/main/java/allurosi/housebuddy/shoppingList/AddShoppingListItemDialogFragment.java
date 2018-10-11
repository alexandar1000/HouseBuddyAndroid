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

    public AddShoppingListItemDialogFragment() {}

    public static AddShoppingListItemDialogFragment newInstance(String title) {
        AddShoppingListItemDialogFragment fragment = new AddShoppingListItemDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
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

        mCancelAddSItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);





//         Show soft keyboard automatically and request focus to field
//        mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

}
