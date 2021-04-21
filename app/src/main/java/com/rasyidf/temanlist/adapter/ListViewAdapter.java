package com.rasyidf.temanlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rasyidf.temanlist.R;
import com.rasyidf.temanlist.TemanDetailActivity;
import com.rasyidf.temanlist.database.Teman;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Teman> contactList;
    private List<Teman> contactListFiltered;
    private TemanAdapterListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.nama);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTemanSelected(contactListFiltered.get(getAdapterPosition()));
                }
            });


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Teman Teman = contactListFiltered.get(getAdapterPosition());
                    PopupMenu popup = new PopupMenu(view.getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_teman, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(popupMenuItem -> {
                                switch (popupMenuItem.getItemId()) {

                                    case R.id.mnuEdit:
                                        Intent i = new Intent(view.getContext(), TemanDetailActivity.class);
                                        Bundle b = new Bundle();
                                        b.putString("nama", Teman.Nama);
                                        i.putExtras(b);
                                        startActivity(view.getContext(), i, b);
                                        break;
                                    case R.id.mnuDelete:

                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            }
                    );
                }
            });
        }
    }


    public ListViewAdapter(Context context, List<Teman> contactList, TemanAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_teman, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Teman teman = contactListFiltered.get(position);
        holder.name.setText(teman.Nama);
    }

    @Override
    public int getItemCount() {
        if (contactListFiltered == null) return 0;
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    contactListFiltered = contactList;
                } else {
                    List<Teman> filteredList = new ArrayList<>();
                    for (Teman row : contactList) {

                        if (row.Nama.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<Teman>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }



    public interface TemanAdapterListener {
        void onTemanSelected(Teman contact);
    }
}
