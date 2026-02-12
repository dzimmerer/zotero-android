package org.zotero.android.pdf.reader.modes

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.pdf.reader.PdfReaderGeminiChatPanel
import org.zotero.android.pdf.reader.PdfReaderPspdfKitBox
import org.zotero.android.pdf.reader.PdfReaderVMInterface
import org.zotero.android.pdf.reader.PdfReaderViewState
import org.zotero.android.pdf.reader.pdfsearch.PdfReaderSearchScreen
import org.zotero.android.pdf.reader.pdfsearch.PdfReaderSearchViewModel
import org.zotero.android.pdf.reader.pdfsearch.PdfReaderSearchViewState
import org.zotero.android.pdf.reader.sidebar.PdfReaderSidebar
import org.zotero.android.pdf.reader.sidebar.SidebarDivider

@Composable
internal fun PdfReaderPhoneMode(
    vMInterface: PdfReaderVMInterface,
    viewState: PdfReaderViewState,
    pdfReaderSearchViewState: PdfReaderSearchViewState,
    pdfReaderSearchViewModel: PdfReaderSearchViewModel,
    annotationsLazyListState: LazyListState,
    thumbnailsLazyListState: LazyListState,
    layoutType: CustomLayoutSize.LayoutType,
    uri: Uri,
) {
    val density = LocalDensity.current
    var panelWidthDp by rememberSaveable { mutableFloatStateOf(320f) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minPanelWidthDp = 240f
        val maxPanelWidthDp = (maxWidth.value * 0.8f).coerceAtLeast(minPanelWidthDp)
        val clampedPanelWidthDp = panelWidthDp.coerceIn(minPanelWidthDp, maxPanelWidthDp)

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                PdfReaderPspdfKitBox(
                    uri = uri,
                    viewState = viewState,
                    vMInterface = vMInterface
                )
                AnimatedContent(
                    targetState = viewState.showSideBar,
                    transitionSpec = {
                        pdfReaderSidebarTransitionSpec()
                    }, label = ""
                ) { showSideBar ->
                    if (showSideBar) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        //Prevent tap to be propagated to composables behind this screen.
                                    }
                                }) {
                            PdfReaderSidebar(
                                viewState = viewState,
                                vMInterface = vMInterface,
                                annotationsLazyListState = annotationsLazyListState,
                                thumbnailsLazyListState = thumbnailsLazyListState,
                                layoutType = layoutType,
                            )
                        }
                    }
                }
                AnimatedContent(targetState = viewState.showPdfSearch, transitionSpec = {
                    pdfReaderPdfSearchTransitionSpec()
                }, label = "") { showScreen ->
                    if (showScreen) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            PdfReaderSearchScreen(
                                onBack = vMInterface::hidePdfSearch,
                                viewModel = pdfReaderSearchViewModel,
                                viewState = pdfReaderSearchViewState,
                            )
                        }
                    }
                }
            }
            if (viewState.showGeminiChat) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .pointerInput(maxPanelWidthDp) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consumeAllChanges()
                                panelWidthDp = (panelWidthDp - (dragAmount / density.density))
                                    .coerceIn(minPanelWidthDp, maxPanelWidthDp)
                            }
                        }
                ) {
                    SidebarDivider(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                    )
                }
                PdfReaderGeminiChatPanel(
                    viewState = viewState,
                    viewModel = vMInterface,
                    modifier = Modifier.width(clampedPanelWidthDp.dp)
                )
            }
        }
    }
}
