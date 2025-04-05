package com.example.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.data.Booking
import com.example.project.databinding.ItemRequestCardBinding

class BookingRequestAdapter(
    private val requests: MutableList<Booking>,
    private val onActionClick: (Booking, String) -> Unit
) : RecyclerView.Adapter<BookingRequestAdapter.BookingViewHolder>() {

    fun updateRequests(newRequests: MutableList<Booking>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemRequestCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    inner class BookingViewHolder(private val binding: ItemRequestCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            with(binding) {
                requestIdText.text = "Booking #${booking.bookingid}"
                userNameText.text = "User ID: ${booking.userid}"
                roomNameText.text = "Room ID: ${booking.roomid}"
                timeText.text = "Time: ${booking.starttime ?: "N/A"}"

                approveButton.setOnClickListener {
                    onActionClick(booking, "Approved")
                }

                rejectButton.setOnClickListener {
                    onActionClick(booking, "Rejected")
                }
            }
        }
    }
}