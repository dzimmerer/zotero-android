package org.zotero.android.pdf.reader

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings

@Composable
internal fun PdfReaderGeminiChatPanel(
    viewState: PdfReaderViewState,
    viewModel: PdfReaderVMInterface,
    modifier: Modifier = Modifier,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var providerDraft by remember { mutableStateOf(viewState.geminiModelProvider) }
    var modelDraft by remember { mutableStateOf(viewState.geminiSelectedModel) }
    var geminiApiKeyDraft by remember { mutableStateOf(viewState.geminiApiKey) }
    var openRouterApiKeyDraft by remember { mutableStateOf(viewState.openRouterApiKey) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Strings.pdf_reader_gemini_chat),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = viewModel::hideGeminiChat) {
                Icon(
                    painter = painterResource(Drawables.ic_close_24dp),
                    contentDescription = stringResource(Strings.pdf_reader_gemini_close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = "${viewState.geminiModelProvider.name}: ${viewState.geminiSelectedModel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GeminiConversationView(
            messages = viewState.geminiMessages,
            isDark = viewState.isDark,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        if (viewState.geminiIsSending) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        if (!viewState.geminiError.isNullOrBlank()) {
            Text(
                text = viewState.geminiError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewState.geminiInput,
                onValueChange = viewModel::onGeminiInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Strings.pdf_reader_gemini_ask_label)) },
                maxLines = 4
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    providerDraft = viewState.geminiModelProvider
                    modelDraft = viewState.geminiSelectedModel
                    geminiApiKeyDraft = viewState.geminiApiKey
                    openRouterApiKeyDraft = viewState.openRouterApiKey
                    showSettingsDialog = true
                }) {
                    Icon(
                        painter = painterResource(Drawables.settings_24px),
                        contentDescription = stringResource(Strings.pdf_reader_gemini_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Button(onClick = viewModel::onGeminiClearHistory) {
                    Text(stringResource(Strings.pdf_reader_gemini_clear))
                }
                Button(
                    onClick = viewModel::onGeminiSend,
                    enabled = !viewState.geminiIsSending
                ) {
                    Text(stringResource(Strings.pdf_reader_gemini_send))
                }
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(stringResource(Strings.pdf_reader_gemini_settings)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderPicker(
                            selectedProvider = providerDraft,
                            onProviderSelected = {
                                providerDraft = it
                                if (!GeminiChatModels.supported(it).contains(modelDraft)) {
                                    modelDraft = GeminiChatModels.defaultModel(it)
                                }
                            }
                        )
                        ModelPicker(
                            selectedModel = modelDraft,
                            models = GeminiChatModels.supported(providerDraft),
                            onModelSelected = { modelDraft = it }
                        )
                        OutlinedTextField(
                            value = geminiApiKeyDraft,
                            onValueChange = { geminiApiKeyDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(Strings.pdf_reader_gemini_api_key)) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = openRouterApiKeyDraft,
                            onValueChange = { openRouterApiKeyDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(Strings.pdf_reader_openrouter_api_key)) },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onGeminiProviderSelected(providerDraft)
                        viewModel.onGeminiModelSelected(modelDraft)
                        viewModel.onGeminiApiKeyChanged(geminiApiKeyDraft.trim())
                        viewModel.onOpenRouterApiKeyChanged(openRouterApiKeyDraft.trim())
                        showSettingsDialog = false
                    }) {
                        Text(stringResource(Strings.pdf_reader_gemini_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text(stringResource(Strings.pdf_reader_gemini_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun ModelPicker(
    selectedModel: String,
    models: List<String>,
    onModelSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedModel)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProviderPicker(
    selectedProvider: ChatModelProvider,
    onProviderSelected: (ChatModelProvider) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedProvider.name)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ChatModelProvider.entries.forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider.name) },
                    onClick = {
                        onProviderSelected(provider)
                        expanded = false
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GeminiConversationView(
    messages: List<GeminiChatMessage>,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val messagesJson = remember(messages) {
        JSONArray().apply {
            messages.forEach { message ->
                put(
                    JSONObject().apply {
                        put("role", if (message.role == GeminiChatRole.User) "user" else "assistant")
                        put("content", message.content)
                    }
                )
            }
        }.toString()
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                isVerticalScrollBarEnabled = true
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            val jsonStringLiteral = JSONObject.quote(messagesJson)
            webView.loadDataWithBaseURL(
                "https://localhost/",
                conversationHtml(jsonStringLiteral, isDark),
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

private fun conversationHtml(messagesJsonLiteral: String, isDark: Boolean): String {
    val bodyBg = if (isDark) "#121212" else "#ffffff"
    val bodyColor = if (isDark) "#e7e7e7" else "#111111"
    val userBg = if (isDark) "#15324a" else "#edf4ff"
    val assistantBg = if (isDark) "#262626" else "#f7f7f7"
    val preBg = if (isDark) "#0f0f0f" else "#111111"
    val preColor = if (isDark) "#f5f5f5" else "#f5f5f5"
    val linkColor = if (isDark) "#7ab8ff" else "#0b57d0"
    return """
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.css"/>
  <link rel="stylesheet" href="https://unpkg.com/katex@0.16.11/dist/katex.min.css"/>
  <style>
    body { font-family: sans-serif; margin: 0; padding: 0; background: $bodyBg; color: $bodyColor; }
    .wrap { padding: 8px; }
    .row { margin: 8px 0; border-radius: 8px; padding: 8px 10px; white-space: normal; overflow-wrap: anywhere; }
    .user { background: $userBg; }
    .assistant { background: $assistantBg; }
    pre { background: $preBg; color: $preColor; border-radius: 8px; padding: 8px; overflow-x: auto; }
    code { font-family: monospace; }
    a { color: $linkColor; }
  </style>
</head>
<body>
  <div id="root" class="wrap"></div>
  <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/contrib/auto-render.min.js"></script>
  <script src="https://unpkg.com/marked/marked.min.js"></script>
  <script src="https://unpkg.com/katex@0.16.11/dist/katex.min.js"></script>
  <script src="https://unpkg.com/katex@0.16.11/dist/contrib/auto-render.min.js"></script>
  <script>
    const data = JSON.parse($messagesJsonLiteral);
    const root = document.getElementById('root');
    for (const item of data) {
      const row = document.createElement('div');
      row.className = 'row ' + (item.role === 'user' ? 'user' : 'assistant');
      if (item.role === 'assistant' && window.marked) {
        row.innerHTML = marked.parse(item.content || '');
        if (window.renderMathInElement) {
          try {
            renderMathInElement(row, {
              delimiters: [
                {left: '$$', right: '$$', display: true},
                {left: '$', right: '$', display: false},
                {left: '\\\\(', right: '\\\\)', display: false},
                {left: '\\\\[', right: '\\\\]', display: true}
              ]
            });
          } catch (e) {}
        }
      } else {
        row.textContent = item.content || '';
      }
      root.appendChild(row);
    }
    window.scrollTo(0, document.body.scrollHeight);
  </script>
</body>
</html>
"""
}
