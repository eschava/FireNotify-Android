package com.eschava.firenotify

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        actionBar?.setLogo(R.drawable.application_icon)
//        actionBar?.setDisplayUseLogoEnabled(true)

        val fcmKey = getString(R.string.fcm_key)
        keyTextView.setText(fcmKey)

        copyKeyButton.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Key", fcmKey)
            clipboard.primaryClip = clip
            Toast.makeText(this, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
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
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Token", token)
            clipboard.primaryClip = clip
            Toast.makeText(this, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
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
