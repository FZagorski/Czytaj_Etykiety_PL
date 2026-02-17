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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                Toast.makeText(
                    this,
                    "Aplikacja potrzebuje dostępu do kamery do skanowania kodów kreskowych",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
    val productViewModel: ProductViewModel = viewModel()
    val allergenViewModel: AllergensViewModel = viewModel()
    val product by productViewModel.product.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()
    val selectedAllergens by allergenViewModel.selectedAllergens.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity
    var scannedCode by remember { mutableStateOf("") }
    var showAllergenMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        allergenViewModel.loadUserPreferences(context)
    }

    LaunchedEffect(scannedCode) {
        if (scannedCode.isNotEmpty()) {
            delay(500)
            productViewModel.fetchProduct(scannedCode)
        }
    }

    val productSafety = remember(product, selectedAllergens) {
        if (product != null) {
            allergenViewModel.checkProductSafety(product?.allergens)
        } else {
            null
        }
    }

    if (showAllergenMenu) {
        AllergensScreen(
            selectedAllergens = selectedAllergens,
            onToggleAllergen = { allergen ->
                allergenViewModel.toggleAllergen(allergen, context)
            },
            onClose = { showAllergenMenu = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Czytaj Etykiety",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                text = "SKANUJ KOD",
                onClick = {
                    scannedCode = ""
                    productViewModel.clearProduct()
                    activity?.startBarcodeScanner { result ->
                        scannedCode = result
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                text = "ALERGENY",
                onClick = { showAllergenMenu = true }
            )


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
                            productViewModel.clearProduct()
                        }
                    )
                }

                product != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SafetyIndicator(productSafety)
                        Spacer(modifier = Modifier.height(16.dp))

                        ProductCard(
                            product = product!!,
                            productSafety = productSafety,
                            onClear = {
                                scannedCode = ""
                                productViewModel.clearProduct()
                            }
                        )
                    }
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
}

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SafetyIndicator(productSafety: AllergensManager.ProductSafety?) {
    when (productSafety) {
        is AllergensManager.ProductSafety.SAFE -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "BEZPIECZNY - Nie zawiera twoich alergenów",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        is AllergensManager.ProductSafety.UNSAFE -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "NIEBEZPIECZNY - Zawiera twoje alergeny!",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Wykryte alergeny: ${productSafety.allergens.joinToString(", ")}",
                        fontSize = 14.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }

        is AllergensManager.ProductSafety.UNKNOWN -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF757575).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "NIEZNANY - Brak informacji o alergenach",
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        null -> {
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    productSafety: AllergensManager.ProductSafety?,
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Składniki:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = product.getDisplayIngredients(),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 18.sp
            )

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
                text = "Błąd",
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Czytaj_EtykietyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}