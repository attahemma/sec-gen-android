package com.swaytech.secgen

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swaytech.secgen.ui.theme.ThemeSecGen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import coil.compose.rememberAsyncImagePainter
import kotlinx.parcelize.Parcelize
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThemeSecGen {
                Surface(color = MaterialTheme.colors.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var screen by rememberSaveable { mutableStateOf("splash") }
    var showIntro by rememberSaveable { mutableStateOf<Boolean?>(null) }

    when (screen) {
        "splash" -> SplashScreen(
            onTimeout = { intro ->
                showIntro = intro
                screen = if (intro) "intro" else "main"
            }
        )
        "intro" -> {
            val context = LocalContext.current // Capture context here
            IntroScreen(onContinue = {
                val prefs = context.getSharedPreferences("secgen_prefs", Context.MODE_PRIVATE)
                prefs.edit { putBoolean("intro_shown", true) } // Use apply instead of commit
                screen = "main"
            })
        }
        "main" -> PermissionAndMediaScreen()
    }
}

@Composable
fun SplashScreen(onTimeout: (Boolean) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("secgen_prefs", Context.MODE_PRIVATE)
        val showIntro = !prefs.getBoolean("intro_shown", false)
        delay(1500)
        onTimeout(showIntro)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("SecGen", style = MaterialTheme.typography.h4)
        }
    }
}

@Composable
fun IntroScreen(onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to SecGen!", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Find and play your audio and video files easily.", style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onContinue) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun PermissionAndMediaScreen() {
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    if (hasPermission) {
        MediaFileListScreen()
    } else {
        Text("Permission required to access media files.")
    }
}

//@Composable
//fun MediaFileListScreen() {
//    val context = LocalContext.current
//    var mediaFiles by remember { mutableStateOf(listOf<MediaFile>()) }
//    var selectedFile by rememberSaveable { mutableStateOf<MediaFile?>(null) }
//
//    LaunchedEffect(Unit) {
//        mediaFiles = getMediaFiles(context.contentResolver)
//    }
//
//    if (selectedFile != null) {
//        MediaPlayerScreen(
//            file = selectedFile!!,
//            allFiles = mediaFiles,
//            onClose = { selectedFile = null }
//        )
//    } else if (mediaFiles.isEmpty()) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(32.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Icon(
//                    imageVector = Icons.Default.VideoLibrary,
//                    contentDescription = "No media",
//                    tint = Color(0xFFBDBDBD),
//                    modifier = Modifier.size(72.dp)
//                )
//                Spacer(modifier = Modifier.height(24.dp))
//                Text(
//                    text = "No media files found",
//                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Your audio and video files will appear here once available.",
//                    style = MaterialTheme.typography.body2.copy(color = Color(0xFF757575)),
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//    } else {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            items(mediaFiles) { file ->
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(20.dp))
//                        .background(Color(0xFFFFF9C4))
//                        .clickable { selectedFile = file }
//                        .padding(12.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        // Thumbnail
//                        if (file.type == "Video" && file.thumbnailUri != null) {
//                            Image(
//                                painter = rememberAsyncImagePainter(
//                                    model = file.thumbnailUri,
//                                    placeholder = painterResource(R.drawable.media_placeholder),
//                                    error = painterResource(R.drawable.error_placeholder)
//                                ),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .size(56.dp)
//                                    .clip(RoundedCornerShape(12.dp)),
//                                contentScale = ContentScale.Crop
//                            )
//                        } else {
//                            Box(
//                                modifier = Modifier
//                                    .size(56.dp)
//                                    .clip(RoundedCornerShape(12.dp))
//                                    .background(Color(0xFFE3F2FD)),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Audiotrack,
//                                    contentDescription = "Audio",
//                                    tint = Color(0xFF1B5E20),
//                                    modifier = Modifier.size(32.dp)
//                                )
//                            }
//                        }
//
//                        // Title and type
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = file.displayName.substringBeforeLast('.'),
//                                style = TextStyle(
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = Color(0xFF4A148C)
//                                ),
//                                maxLines = 1,
//                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Box(
//                                modifier = Modifier
//                                    .clip(RoundedCornerShape(6.dp))
//                                    .background(Color(0xFF1E88E5))
//                                    .padding(horizontal = 8.dp, vertical = 3.dp)
//                            ) {
//                                Text(
//                                    text = file.type,
//                                    style = TextStyle(
//                                        color = Color.White,
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                )
//                            }
//                        }
//
//                        // Play button
//                        Box(
//                            modifier = Modifier
//                                .size(44.dp)
//                                .clip(RoundedCornerShape(10.dp))
//                                .background(
//                                    brush = Brush
//                                        .linearGradient(
//                                            listOf(
//                                                Color(0xFF1E88E5),
//                                                Color(0xFF00BCD4),
//                                                Color(0xFF26A69A)
//                                            )
//                                        ),
//                                    shape = RoundedCornerShape(10.dp)
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.PlayArrow,
//                                contentDescription = "Play",
//                                tint = Color.White,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun MediaFileListScreen() {
    val context = LocalContext.current
    var mediaFiles by remember { mutableStateOf(listOf<MediaFile>()) }
    var selectedFile by rememberSaveable { mutableStateOf<MediaFile?>(null) }

    LaunchedEffect(Unit) {
        mediaFiles = getMediaFiles(context.contentResolver)
    }

    if (selectedFile != null) {
        MediaPlayerScreen(
            file = selectedFile!!,
            allFiles = mediaFiles,
            onClose = { selectedFile = null }
        )
    } else if (mediaFiles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "No media",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No media files found",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your audio and video files will appear here once available.",
                    style = MaterialTheme.typography.body2.copy(color = Color(0xFF757575)),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mediaFiles) { file ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF9C4))
                        .clickable { selectedFile = file }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thumbnail
                        if (file.type == "Video" && file.thumbnailUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = file.thumbnailUri,
                                    placeholder = painterResource(R.drawable.media_placeholder),
                                    error = painterResource(R.drawable.error_placeholder)
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = "Audio",
                                    tint = Color(0xFF1B5E20),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Title and type
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.displayName.substringBeforeLast('.'),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4A148C)
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E88E5))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = file.type,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Play button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush
                                        .linearGradient(
                                            listOf(
                                                Color(0xFF1E88E5),
                                                Color(0xFF00BCD4),
                                                Color(0xFF26A69A)
                                            )
                                        ),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerScreen(file: MediaFile, allFiles: List<MediaFile>, onClose: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(file.uri))
            prepare()
            playWhenReady = true
        }
    }
    var currentFile by remember { mutableStateOf(file) }
    var isLandscape by rememberSaveable { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    val activity = (context as? android.app.Activity)
    val view = LocalView.current

    // For video: Hide status bar and use sensor orientation
    LaunchedEffect(currentFile.type) {
        activity?.let {
            val window = it.window
            if (currentFile.type == "Video") {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowCompat.getInsetsController(window, view)
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.statusBars())
                it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    fun playFile(newFile: MediaFile) {
        currentFile = newFile
        exoPlayer.setMediaItem(MediaItem.fromUri(newFile.uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun playNext() {
        val audioFiles = allFiles.filter { it.type == "Audio" }
        val currentIndex = audioFiles.indexOfFirst { it.uri == currentFile.uri }
        if (currentIndex < audioFiles.size - 1) {
            playFile(audioFiles[currentIndex + 1])
        }
    }

    fun playPrevious() {
        val audioFiles = allFiles.filter { it.type == "Audio" }
        val currentIndex = audioFiles.indexOfFirst { it.uri == currentFile.uri }
        if (currentIndex > 0) {
            playFile(audioFiles[currentIndex - 1])
        }
    }

    LaunchedEffect(Unit) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    if (currentFile.type == "Audio") {
                        playNext()
                    }
                }
            }
        }
        exoPlayer.addListener(listener)

        while (true) {
            isPlaying = exoPlayer.isPlaying
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration
            kotlinx.coroutines.delay(100)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity?.let {
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                val controller = WindowCompat.getInsetsController(it.window, view)
                controller.show(WindowInsetsCompat.Type.statusBars())
                it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    val onBackPressed = {
        exoPlayer.stop()
        activity?.let {
            WindowCompat.setDecorFitsSystemWindows(it.window, true)
            val controller = WindowCompat.getInsetsController(it.window, view)
            controller.show(WindowInsetsCompat.Type.statusBars())
            it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        onClose()
    }

    BackHandler(enabled = true) {
        onBackPressed()
    }

    if (currentFile.type == "Video") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        useController = true
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = {
                    isLandscape = !isLandscape
                    activity?.requestedOrientation = if (isLandscape) {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isLandscape) Icons.Default.ScreenLockPortrait else Icons.Default.ScreenLockLandscape,
                    contentDescription = if (isLandscape) "Switch to Portrait" else "Switch to Landscape",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Now Playing") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    modifier = Modifier.background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF1E88E5),
                                Color(0xFF00BCD4),
                                Color(0xFF26A69A)
                            )
                        )
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Blurred background using thumbnail with semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)) // Semi-transparent overlay for blur effect
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = currentFile.thumbnailUri,
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f), // Lower opacity to simulate blur
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(300.dp) // Increased size
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1E88E5),
                                        Color(0xFF00BCD4),
                                        Color(0xFF26A69A)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = currentFile.thumbnailUri ?: R.drawable.media_placeholder,
                                placeholder = painterResource(R.drawable.media_placeholder),
                                error = painterResource(R.drawable.error_placeholder)
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(280.dp) // Slightly smaller to show gradient border
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = currentFile.displayName.substringBeforeLast('.'),
                        style = MaterialTheme.typography.h6.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF26A69A)
                        ),
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    // Seek bar
                    if (duration > 0L) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            Slider(
                                value = currentPosition.coerceAtMost(duration).toFloat(),
                                onValueChange = { value ->
                                    exoPlayer.seekTo(value.toLong())
                                },
                                valueRange = 0f..duration.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF26A69A),
                                    activeTrackColor = Color(0xFF26A69A)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatMillis(currentPosition),
                                    style = MaterialTheme.typography.caption
                                )
                                Text(
                                    text = formatMillis(duration),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Control bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF9C4))
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous file button
                            IconButton(
                                onClick = { playPrevious() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Rewind button
                            IconButton(
                                onClick = { exoPlayer.seekTo(exoPlayer.currentPosition - 10000) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.FastRewind,
                                    contentDescription = "Rewind",
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Play/Pause button
                            IconButton(
                                onClick = {
                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Forward button
                            IconButton(
                                onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10000) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.FastForward,
                                    contentDescription = "Forward",
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Next file button
                            IconButton(
                                onClick = { playNext() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = Color(0xFF26A69A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


fun getMediaFiles(contentResolver: ContentResolver): List<MediaFile> {
    val files = mutableListOf<MediaFile>()
    val audioProjection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME
    )
    val videoProjection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME
    )

    val audioCursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        audioProjection,
        null,
        null,
        null
    )
    audioCursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
            files.add(MediaFile(uri, name, "Audio", null))
        }
    }

    val videoCursor = contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        videoProjection,
        null,
        null,
        null
    )
    videoCursor?.use {
        val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val name = it.getString(nameCol)
            val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
            val thumbUri = Uri.withAppendedPath(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, id.toString())
            files.add(MediaFile(uri, name, "Video", thumbUri))
        }
    }
    return files
}

fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Parcelize
data class MediaFile(
    val uri: Uri,
    val displayName: String,
    val type: String,
    val thumbnailUri: Uri?
) : Parcelable

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ThemeSecGen {
        PermissionAndMediaScreen()
    }
}

