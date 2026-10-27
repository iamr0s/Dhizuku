package com.rosan.dhizuku.ui.page.settings.oss

import android.content.Context

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.automirrored.twotone.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.openUrlInBrowser
import com.rosan.dhizuku.ui.theme.exclude

import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
private data class OssLicense(
    val name: String,
    val url: String
)

@Immutable
private data class OssLibrary(
    val id: String,
    val name: String,
    val version: String,
    val subtitle: String,
    val source: String,
    val licenses: List<OssLicense>,
    val usedBy: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicensesPage(
    windowInsets: WindowInsets,
    navController: NavController
) {
    val context = LocalContext.current
    val libraries by produceState<List<OssLibrary>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { loadLibraries(context) }
    }
    var query by rememberSaveable { mutableStateOf("") }
    val allLibraries = libraries.orEmpty()
    val filtered = remember(allLibraries, query) {
        if (query.isBlank()) allLibraries
        else allLibraries.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.source.contains(query, ignoreCase = true) ||
                it.licenses.any { license -> license.name.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = null)
                    }
                },
                title = { Text(stringResource(R.string.oss_licenses)) }
            )
        }
    ) { padding ->
        if (libraries == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true
                )
                LibraryList(
                    libraries = filtered,
                    modifier = Modifier.weight(1f),
                    onOpen = { library ->
                        val url = library.source.ifBlank {
                            library.licenses.firstOrNull()?.url.orEmpty()
                        }
                        if (url.isNotBlank()) runCatching { context.openUrlInBrowser(url) }
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryList(
    libraries: List<OssLibrary>,
    modifier: Modifier = Modifier,
    onOpen: (OssLibrary) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = libraries,
            key = { it.id },
            contentType = { 0 }
        ) { library ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(library) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        library.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        library.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (library.usedBy.isNotEmpty()) {
                        val direct = stringResource(R.string.app_name)
                        Text(
                            stringResource(
                                R.string.used_by,
                                library.usedBy.joinToString(", ") {
                                    if (it == "__app__") direct else it
                                }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.TwoTone.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
        }
    }
}

private fun loadLibraries(context: Context): List<OssLibrary> {
    val json = context.resources.openRawResource(R.raw.licenses)
        .bufferedReader()
        .use { it.readText() }
    val root = JSONObject(json)
    val dependencyRoot = JSONObject(
        context.resources.openRawResource(R.raw.dependencies)
            .bufferedReader()
            .use { it.readText() }
    )
    val parentsObject = dependencyRoot.optJSONObject("parents") ?: JSONObject()
    val parents = buildMap {
        for (key in parentsObject.keys()) {
            val array = parentsObject.optJSONArray(key) ?: continue
            put(
                key,
                buildList {
                    for (i in 0 until array.length()) {
                        array.optString(i).takeIf { it.isNotBlank() }?.let { add(it) }
                    }
                }
            )
        }
    }
    val nameById = buildMap {
        val libraries = root.optJSONArray("libraries") ?: JSONArray()
        for (i in 0 until libraries.length()) {
            val library = libraries.optJSONObject(i) ?: continue
            val id = library.optString("uniqueId")
            if (id.isNotBlank()) put(id, library.optString("name"))
        }
    }
    fun parentName(parent: String): String {
        if (parent == "__app__") return parent
        return nameById[parent]
            ?: nameById[parent.removeSuffix("-android").removeSuffix("-jvm")]
            ?: parent.substringAfter(':')
    }
    val array = root.optJSONArray("libraries") ?: JSONArray()
    val raw = buildList {
        for (i in 0 until array.length()) {
            val library = array.optJSONObject(i) ?: continue
            val licenseArray = library.optJSONArray("licenses") ?: JSONArray()
            val libraryLicenses = buildList {
                for (j in 0 until licenseArray.length()) {
                    val license = licenseArray.optJSONObject(j) ?: continue
                    add(
                        OssLicense(
                            name = license.optString("name"),
                            url = license.optString("url")
                        )
                    )
                }
            }
            val source = library.optString("website").ifBlank {
                library.optJSONObject("scm")?.optString("url").orEmpty()
            }
            add(
                OssLibrary(
                    id = library.optString("uniqueId"),
                    name = library.optString("name"),
                    version = library.optString("artifactVersion"),
                    subtitle = "",
                    source = source,
                    licenses = libraryLicenses,
                    usedBy = parents[library.optString("uniqueId")].orEmpty()
                        .map(::parentName)
                        .distinct()
                )
            )
        }
    }

    val merged = LinkedHashMap<String, OssLibrary>()
    for (library in raw) {
        val key = "${library.name.lowercase()}\u0000${library.version}"
        val existing = merged[key]
        merged[key] = if (existing == null) library else existing.copy(
            source = existing.source.ifBlank { library.source },
            licenses = (existing.licenses + library.licenses)
                .distinctBy { it.name to it.url },
            usedBy = (existing.usedBy + library.usedBy)
                .filter { it != existing.name }
                .distinct()
        )
    }

    return merged.values.map { library ->
        library.copy(
            subtitle = listOfNotNull(
                library.version.takeIf { it.isNotBlank() },
                library.licenses.joinToString(", ") { it.name }.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
        )
    }.sortedBy { it.name.lowercase() }
}
