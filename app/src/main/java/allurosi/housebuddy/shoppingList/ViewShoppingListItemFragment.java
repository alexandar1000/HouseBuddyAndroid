package allurosi.housebuddy.shoppingList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import allurosi.housebuddy.R;

public class ViewShoppingListItemFragment extends Fragment implements EditShoppingListItemDialogFragment.EditShoppingListItemListener {

    private TextView mViewName;
    private TextView mViewInfo;

    private Context mContext;
    private ActionBar mActionBar;

    private Integer position;

    private View view;

    private ViewShoppingListItemFragmentListener listener;

    public interface ViewShoppingListItemFragmentListener {
        void onDeleteShoppingItem(int itemToDelete);

        void onEditShoppingItem(ShoppingItem editedItem, int position, ViewShoppingListItemFragment vf);

        void onCloseViewShoppingItem();
    }

//    public ViewShoppingListItemFragment() {}

    public static ViewShoppingListItemFragment newInstance(String title, String name, String info, int position) {
        ViewShoppingListItemFragment fragment = new ViewShoppingListItemFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_view_shopping_list_item, container, false);
        view = rootView;

        mViewName = (TextView) rootView.findViewById(R.id.shoppingItemNamePreview);
        mViewInfo = (TextView) rootView.findViewById(R.id.shoppingItemInfoPreview);

        this.position = Integer.parseInt((String) getArguments().get("position"));

        //populate the corresponding values
        if (getArguments() != null && getArguments().size() > 1) {
            mViewName.setText(getArguments().getString("name"));
            mViewInfo.setText(getArguments().getString("info"));
        }

        return rootView;
    }

    @Override
    public void onFinishEditShoppingListItem(ShoppingItem shoppingItem) {
        listener.onEditShoppingItem(shoppingItem, position, this);

        updateText(shoppingItem.getName(), shoppingItem.getInfo());

        System.out.println(mViewName.getText().toString());
        System.out.println(mViewInfo.getText().toString());

        Toast.makeText(mContext, getString(R.string.saved_shopping_item), Toast.LENGTH_SHORT).show();
    }

    private void updateText(String name, String info) {
        mViewName.setText(name);
        mViewInfo.setText(info);
    }

    @Override
    public void onCloseEditShoppingListItemDialog() {

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_shopping_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                listener.onCloseViewShoppingItem();
                return true;

            case R.id.action_complete_shopping_item:

                CharSequence text = "EDIT";
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                listener.onCloseViewShoppingItem();
                return true;

            case R.id.action_edit_shopping_item:
                EditShoppingListItemDialogFragment editShoppingListItemDialogFragment = EditShoppingListItemDialogFragment.newInstance("Edit Item", mViewName.getText().toString(), mViewInfo.getText().toString(), position);
                editShoppingListItemDialogFragment.setListener(this);

                mActionBar.setTitle("Edit Shopping Item");

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.flContent, editShoppingListItemDialogFragment).addToBackStack(null).commit();
                return true;

            case R.id.action_delete_shopping_item:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    builder = new AlertDialog.Builder(mContext, android.R.style.ThemeOverlay_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(mContext);
                }

                builder.setMessage(getResources().getQuantityString(R.plurals.delete_shopping_item_question, 1))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete task
                                listener.onDeleteShoppingItem(position);
                                listener.onCloseViewShoppingItem();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void setListener(ShoppingListFragment parent) {
        listener = (ViewShoppingListItemFragmentListener) parent;
    }

    // Deprecated method to support lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mActionBar = ((AppCompatActivity) activity).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActionBar = ((AppCompatActivity) context).getSupportActionBar();
    }


}
