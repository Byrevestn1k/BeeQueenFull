package com.example.beequeen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class ImageAdapter(
    private var items: MutableList<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<ImageAdapter.VH>() {

    fun setData(newItems: List<File>) { items = newItems.toMutableList(); notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return VH(v)
    }
    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = items[position]
        Glide.with(holder.itemView.context).load(f).into(holder.img)
        holder.label.text = TrainingUtils.readLabel(f)
        holder.itemView.setOnClickListener { onClick(f) }
    }
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgThumb)
        val label: TextView = v.findViewById(R.id.tvLabel)
    }
}
