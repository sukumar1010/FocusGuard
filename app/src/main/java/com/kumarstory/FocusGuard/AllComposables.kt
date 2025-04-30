package com.kumarstory.FocusGuard

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.delay
import com.google.accompanist.systemuicontroller.rememberSystemUiController



@SuppressLint("UnrememberedMutableState")
@Composable
fun MainScreen(apps: List<AppData>) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Black,       // Change to any color
            darkIcons = false          // false = white icons, true = dark icons
        )
    }

    var timeMinutes by remember { mutableStateOf("") }
    var remainingTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current

    var isBlockingActive by remember { mutableStateOf(BlockManager.isBlockingActive()) }

    LaunchedEffect(Unit) {
        while (true) {
            isBlockingActive = BlockManager.isBlockingActive()
            delay(1000) // Check every second
        }
    }


    val latestTime = BlockManager.returnLatestTime(context)


    LaunchedEffect(Unit) {
        BlockManager.loadBlockedApps(context)
    }
    val appStates = remember {
        apps.map {
            mutableStateOf(it.copy(isBlocked = BlockManager.blockedApps.contains(it.packageName)))
        }
    }

    LaunchedEffect(BlockManager.blockEndTime) {
        while (BlockManager.isBlockingActive()) {
            remainingTime = BlockManager.getRemainingTime()
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.Black)
    ) {
        Text(
            text="Remaining Time: ${remainingTime / 60000} min ${remainingTime % 60000 / 1000} sec",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(top = 3.dp)
                ,
            color = Color.White

            )
        Spacer(Modifier.height(0.dp))


        var selectedTime by remember { mutableStateOf("00:00") }

        var hour by remember { mutableStateOf(0) }
        var minute by remember { mutableStateOf(0) }


        TimePicker(
            selectedHour = hour,
            selectedMinute = minute,
            onTimeChange = { h, m ->
                hour=h
                minute=m

                timeMinutes = (h * 60 + m ).toString()

                selectedTime = "${h}H : " +
                        "${m}M"

            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text="Timer was set about: $latestTime",
            color = Color.White, fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


        Spacer(Modifier.height(10.dp))

        Button(onClick = {
            if (!isAccessibilityServiceEnabled(BlockAccessibilityService::class.java,context)) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@Button  // Stop further execution
            }


            BlockManager.storeLatestTime(context,selectedTime)
            val mins = timeMinutes.toIntOrNull() ?: 0
            BlockManager.blockEndTime = System.currentTimeMillis() + mins * 60_000
            remainingTime = BlockManager.getRemainingTime()

            Toast.makeText(context, "Selected apps are Unaccessed for for $mins minutes", Toast.LENGTH_LONG).show()

            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)

        },

                enabled = !isBlockingActive
            , modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,       // Background color
                contentColor = Color.LightGray,
                disabledContainerColor = Color.LightGray,    // when disabled
                disabledContentColor = Color.DarkGray
            )
            ) {
            Text("Restrict app Access")
        }

        Spacer(Modifier.height(16.dp))

        appStates.forEach { appState ->
            val app = appState.value
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Image(
                    bitmap = drawableToImageBitmap(app.icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = app.appName,
                    Modifier.weight(1f),
                    color= Color.White
                )

                Switch(
                    checked = app.isBlocked,
                    onCheckedChange = {
                        if (!BlockManager.isBlockingActive()) {  // only allow toggle if Zen Mode is not active
                            appState.value = app.copy(isBlocked = it)
                            if (it) BlockManager.blockedApps.add(app.packageName)
                            else BlockManager.blockedApps.remove(app.packageName)
                            BlockManager.saveBlockedApps(context)
                        }
                    },
                    enabled = !isBlockingActive || app.isBlocked  // allow turning ON/OFF only when not blocking, or allow viewing the ON switch during blocking
                )

            }
        }
    }
}



fun isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>,context: Context): Boolean {
    val expectedComponentName = ComponentName(context, serviceClass)
    val enabledServicesSetting = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    for (service in colonSplitter) {
        if (ComponentName.unflattenFromString(service) == expectedComponentName) {
            return true
        }
    }

    return false
}
@Composable
fun TimePicker(
    selectedHour: Int,
    selectedMinute: Int,

    onTimeChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeUnitPicker(
            range = 0..23,
            selected = selectedHour,
            onValueChange = { onTimeChange(it, selectedMinute) },
            label = "HH"
        )

        Spacer(modifier = Modifier.width(8.dp))

        TimeUnitPicker(
            range = 0..59,
            selected = selectedMinute,
            onValueChange = { onTimeChange(selectedHour, it) },
            label = "MM"
        )

        Spacer(modifier = Modifier.width(8.dp))


    }
}

@Composable
fun TimeUnitPicker(
    range: IntRange,
    selected: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected)

    LaunchedEffect(Unit) {
        listState.scrollToItem(selected)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + if (listState.firstVisibleItemScrollOffset > 50) 1 else 0
            val value = range.first + centerIndex
            if (value in range) {
                onValueChange(value)
            }
        }
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 64.dp)
        ) {
            items(items = range.toList(), key = { it }) { value ->
                val isSelected = value == selected
                Text(
                    text = value.toString().padStart(2, '0'),
                    fontSize = 32.sp,
                    color = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }


        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(48.dp)
                .fillMaxWidth()
                .background(Color(0x33FFFFFF))
        )
    }
}



fun drawableToImageBitmap(drawable: Drawable): androidx.compose.ui.graphics.ImageBitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
        drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}
