package com.example.usbprintertest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.util.Log
import com.example.usbprintertest.util.T
import com.printsdk.usbsdk.UsbDriver

object PrintManager {

    private val TAG = PrintManager::class.java.simpleName

    private val PID11 = 8211
    private val PID13 = 8213
    private val PID15 = 8215
    private val VENDORID = 1305

    private val ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION"

    internal var mUsbDriver: UsbDriver? = null
    internal var mUsbDev1: UsbDevice? = null        //打印机1
    internal var mUsbDev2: UsbDevice? = null        //打印机2
    internal var mUsbDev: UsbDevice? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbReceiver: UsbReceiver? = null

    private var connectedStatus = false

    fun initPrinter(context: Context) {
        try {
            if (mUsbDriver?.isConnected == false) {
                // USB线未连接
                mUsbManager?.deviceList?.map {
                    val device = it.value
                    if (device.productId == PID11 && device.vendorId == VENDORID
                            || device.productId == PID13 && device.vendorId == VENDORID
                            || device.productId == PID15 && device.vendorId == VENDORID) {
                        connectedStatus = mUsbDriver?.usbAttached(device) == true
                        if (!connectedStatus) {
                            return@map
                        }
                        connectedStatus = mUsbDriver?.openUsbDevice(device) == true

                        // 打开设备
                        if (connectedStatus) {
                            if (device.productId == PID11) {
                                mUsbDev1 = device
                                mUsbDev = mUsbDev1
                                Log.d(TAG, "打印机1")
                            } else {
                                mUsbDev2 = device
                                mUsbDev = mUsbDev2
                                Log.d(TAG, "打印机2")
                            }
                            ToastUtils.show(context.getString(R.string.USB_Driver_Success))
                            return@map
                        } else {
                            ToastUtils.show(context.getString(R.string.USB_Driver_Failed))
                            return@map
                        }
                    }
                }
            } else {
                connectedStatus = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show(e.message.toString())
        }
    }

    fun destory() {

    }

    fun printString() {

    }

    fun printQRCode() {

    }

    fun registUSBReceiver(context: Context) {
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        mUsbDriver = UsbDriver(mUsbManager, context)
        val permissionIntent1 = PendingIntent.getBroadcast(context, 0,
                Intent(ACTION_USB_PERMISSION), 0)
        mUsbDriver?.setPermissionIntent(permissionIntent1)
        // Broadcast listen for new devices

        mUsbReceiver = UsbReceiver()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(mUsbReceiver, filter)
    }

    fun unRegistUSBReciever(context: Context) {
        context.unregisterReceiver(mUsbReceiver)
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive action = " + intent.action)
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                if (mUsbDriver?.usbAttached(intent) == true) {
                    Log.d(TAG, "onReceive usbAttached = true")
                    val device = intent
                            .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    Log.d(TAG, "onReceive device.productId = " + device.productId)
                    Log.d(TAG, "onReceive device.vendorId = " + device.vendorId)
                    if (device.productId == PID11 && device.vendorId == VENDORID
                            || device.productId == PID13 && device.vendorId == VENDORID
                            || device.productId == PID15 && device.vendorId == VENDORID) {
                        if (mUsbDriver?.openUsbDevice(device) == true) {
                            Log.d(TAG, "onReceive openUsbDevice")
                            if (device.productId == PID11) {
                                mUsbDev1 = device
                                mUsbDev = mUsbDev1
                            } else {
                                mUsbDev2 = device
                                mUsbDev = mUsbDev2
                            }
                        } else {
                            Log.d(TAG, "onReceive not openUsbDevice")
                        }
                    }
                } else {
                    Log.d(TAG, "onReceive usbAttached = false")
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent
                        .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                if (device.productId == PID11 && device.vendorId == VENDORID
                        || device.productId == PID13 && device.vendorId == VENDORID
                        || device.productId == PID15 && device.vendorId == VENDORID) {
                    mUsbDriver?.closeUsbDevice(device)
                    if (device.productId == PID11)
                        mUsbDev1 = null
                    else
                        mUsbDev2 = null
                }
            } else if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device.productId == PID11 && device.vendorId == VENDORID
                                || device.productId == PID13 && device.vendorId == VENDORID
                                || device.productId == PID15 && device.vendorId == VENDORID) {
                            if (mUsbDriver?.openUsbDevice(device) == true) {
                                if (device.productId == PID11) {
                                    mUsbDev1 = device
                                    mUsbDev = mUsbDev1
                                } else {
                                    mUsbDev2 = device
                                    mUsbDev = mUsbDev2
                                }
                            }
                        }
                    } else {
                        ToastUtils.show("permission denied for device")
                    }
                }
            }
        }
    }
}