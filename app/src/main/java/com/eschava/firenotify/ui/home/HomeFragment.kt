package com.eschava.firenotify.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.eschava.firenotify.BuildConfig
import com.eschava.firenotify.R
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        versionValueLabel.text = BuildConfig.VERSION_NAME

//        actionBar?.setLogo(R.drawable.application_icon)
//        actionBar?.setDisplayUseLogoEnabled(true)

        val fcmKey = getString(R.string.fcm_key)
        keyTextView.setText(fcmKey)

        copyKeyButton.setOnClickListener {
            val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Key", fcmKey)
            clipboard.primaryClip = clip
            Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        shareKeyButton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Key")
            shareIntent.putExtra(Intent.EXTRA_TEXT, fcmKey)
            shareIntent.type = "text/plain"
            startActivity(shareIntent)
        }

        val token = FirebaseInstanceId.getInstance().token
        tokenTextView.setText(token)

        copyTokenButton.setOnClickListener {
            val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Token", token)
            clipboard.primaryClip = clip
            Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        shareTokenButton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Token")
            shareIntent.putExtra(Intent.EXTRA_TEXT, token)
            shareIntent.type = "text/plain"
            startActivity(shareIntent)
        }
    }
}