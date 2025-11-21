package com.example.bank_app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bank_app.R
import com.example.bank_app.databinding.FragmentNotificationsBinding
import com.example.bank_app.ui.Notif
import com.example.bank_app.ui.NotificationAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notifs = listOf(
            Notif(R.drawable.ic_check, "Aucune activité suspecte détectée"),
            Notif(R.drawable.ic_chart, "Solde prévu : 2200 TND (dans 30 jours)"),
            Notif(R.drawable.ic_bulb, "Conseil : Réduire vos dépenses mensuelles de 10%")
        )

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = NotificationAdapter(notifs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}