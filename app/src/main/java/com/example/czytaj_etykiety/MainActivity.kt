package com.example.czytaj_etykiety

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.czytaj_etykiety.ui.theme.Czytaj_EtykietyTheme
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class VerticalActivity : CaptureActivity()

class MainActivity : ComponentActivity() {
    private val scannedCode = mutableStateOf("")

    private val scannerLauncher = registerForActivityResult(ScanContract()){
            result ->
        if (result.contents == null){
            Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        else{
            scannedCode.value = result.contents
        }
    }

    private fun showCamera(){
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Zeskanuj Kod")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.captureActivity = VerticalActivity::class.java
        scannerLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Czytaj_EtykietyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        ScanButton(onScanClick = { showCamera() })

                        Text(
                            text = if (scannedCode.value.isNotEmpty()) "Zeskanowano: ${scannedCode.value}" else "",
                            modifier = Modifier.padding(top = 20.dp),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
    fun ScanButton(onScanClick: () -> Unit) {
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            Text("SKANUJ", fontWeight = FontWeight.Bold, fontSize = 30.sp)
        }
    }

@Preview(showBackground = true)
@Composable
    fun GreetingPreview() {
        Czytaj_EtykietyTheme {
            ScanButton(onScanClick = {})
        }
    }
