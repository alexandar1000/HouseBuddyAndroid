package allurosi.housebuddy.expensetracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import allurosi.housebuddy.R;

public class ExpensesListAdapter extends ArrayAdapter<Product> {

    private Context mContext;
    private int resourceId;
    private List<Product> products;

    public ExpensesListAdapter(Context context, int resourceId, List<Product> products) {
        super(context, resourceId, products);
        this.mContext = context;
        this.resourceId = resourceId;
        this.products = products;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Product product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }

        TextView productName = convertView.findViewById(R.id.item_product_name);
        TextView productPrice = convertView.findViewById(R.id.item_product_price);

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.valueOf(product.getPrice()));
        }

        return convertView;
    }

}
