package com.example.czytaj_etykiety

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.czytaj_etykiety.models.Product
import com.example.czytaj_etykiety.ui.theme.Czytaj_EtykietyTheme
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

class VerticalActivity : CaptureActivity()

class MainActivity : ComponentActivity() {

    private var onBarcodeScannedCallback: ((String) -> Unit)? = null

    private val scannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this@MainActivity, "Anulowano skanowanie", Toast.LENGTH_SHORT).show()
        } else {
            onBarcodeScannedCallback?.invoke(result.contents)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchScanner()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Brak dostępu do kamery. Aplikacja nie może skanować kodów.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun launchScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("🔍 Przytrzymaj kod kreskowy w ramce")
            setCameraId(0)
            setBeepEnabled(false)
            setOrientationLocked(true)
            captureActivity = VerticalActivity::class.java
        }
        scannerLauncher.launch(options)
    }

    fun startBarcodeScanner(onResult: (String) -> Unit) {
        onBarcodeScannedCallback = onResult

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchScanner()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Czytaj_EtykietyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val viewModel: ProductViewModel = viewModel()
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val activity = context as? MainActivity

    var scannedCode by remember { mutableStateOf("") }

    LaunchedEffect(scannedCode) {
        if (scannedCode.isNotEmpty()) {
            delay(500)
            viewModel.fetchProduct(scannedCode)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Czytaj Etykiety",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Skanuj kody produktów spożywczych",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                scannedCode = ""
                viewModel.clearProduct()
                activity?.startBarcodeScanner { result ->
                    scannedCode = result
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "📷 SKANUJ KOD",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (scannedCode.isNotEmpty()) {
            Text(
                text = "Zeskanowano: $scannedCode",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        when {
            isLoading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Szukam produktu...")
                }
            }

            error != null -> {
                ErrorCard(
                    error = error!!,
                    onRetry = {
                        scannedCode = ""
                        viewModel.clearProduct()
                    }
                )
            }

            product != null -> {
                ProductCard(
                    product = product!!,
                    onClear = {
                        scannedCode = ""
                        viewModel.clearProduct()
                    }
                )
            }

            scannedCode.isNotEmpty() -> {
                Text(
                    text = "Przetwarzanie kodu...",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Znaleziono produkt!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product.getDisplayName(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            product.brands?.let { brand ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Marka: $brand",
                    fontSize = 18.sp
                )
            }

            product.quantity?.let { quantity ->
                Text(
                    text = "Ilość: $quantity",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Skanuj kolejny produkt")
            }
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌ Błąd",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = error,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spróbuj ponownie")
            }
        }
    }
}