/**
 * DocMorph Application Frontend JavaScript
 * Handles tab transitions, file previews, conversions, downloads, and error alerts.
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- State ---
    let activeTab = 'text'; // 'text' | 'image'
    let selectedImageFile = null;
    const conversionHistory = [];

    // --- DOM Elements ---
    const tabTextBtn = document.getElementById('tab-text-btn');
    const tabImageBtn = document.getElementById('tab-image-btn');
    const sectionText = document.getElementById('section-text');
    const sectionImage = document.getElementById('section-image');

    // Error Alert
    const panelErrorAlert = document.getElementById('panel-error-alert');
    const panelErrorMessage = document.getElementById('panel-error-message');
    const panelErrorClose = document.getElementById('panel-error-close');

    // Text Tab Elements
    const textDocTitle = document.getElementById('text-doc-title');
    const textInput = document.getElementById('text-input');
    const textWordCount = document.getElementById('text-word-count');
    const textCharCount = document.getElementById('text-char-count');
    const btnInsertSample = document.getElementById('btn-insert-sample');
    const btnClearText = document.getElementById('btn-clear-text');
    const textFormatSelect = document.getElementById('text-format-select');
    const textFormatCards = document.querySelectorAll('#section-text [data-format]');
    const btnConvertText = document.getElementById('btn-convert-text');
    const btnTextLabel = document.getElementById('btn-text-label');
    const spinnerText = document.getElementById('spinner-text');

    // Image Tab Elements
    const imageFileInput = document.getElementById('image-file-input');
    const dropzone = document.getElementById('dropzone');
    const dropzoneEmpty = document.getElementById('dropzone-empty');
    const dropzonePreview = document.getElementById('dropzone-preview');
    const previewThumbnail = document.getElementById('preview-thumbnail');
    const previewFilename = document.getElementById('preview-filename');
    const previewDimensions = document.getElementById('preview-dimensions');
    const previewFilesize = document.getElementById('preview-filesize');
    const btnRemoveImage = document.getElementById('btn-remove-image');
    const imageFormatSelect = document.getElementById('image-format-select');
    const imageFormatCards = document.querySelectorAll('#section-image [data-img-format]');
    const btnConvertImage = document.getElementById('btn-convert-image');
    const btnImageLabel = document.getElementById('btn-image-label');
    const spinnerImage = document.getElementById('spinner-image');

    // History Elements
    const recentConversionsCard = document.getElementById('recent-conversions-card');
    const recentConversionsList = document.getElementById('recent-conversions-list');
    const conversionCountBadge = document.getElementById('conversion-count-badge');

    // Toast Container
    const toastContainer = document.getElementById('toast-container');

    // --- Tab Switching Logic ---
    function switchTab(tab) {
        activeTab = tab;
        hidePanelError();

        if (tab === 'text') {
            tabTextBtn.className = 'flex-1 py-2.5 px-4 rounded-lg text-sm font-semibold transition-all flex items-center justify-center space-x-2 bg-indigo-600 text-white shadow-md shadow-indigo-600/30';
            tabImageBtn.className = 'flex-1 py-2.5 px-4 rounded-lg text-sm font-medium transition-all text-slate-400 hover:text-slate-200 flex items-center justify-center space-x-2';
            sectionText.classList.remove('hidden');
            sectionText.classList.add('animate-fade-in');
            sectionImage.classList.add('hidden');
        } else {
            tabImageBtn.className = 'flex-1 py-2.5 px-4 rounded-lg text-sm font-semibold transition-all flex items-center justify-center space-x-2 bg-indigo-600 text-white shadow-md shadow-indigo-600/30';
            tabTextBtn.className = 'flex-1 py-2.5 px-4 rounded-lg text-sm font-medium transition-all text-slate-400 hover:text-slate-200 flex items-center justify-center space-x-2';
            sectionImage.classList.remove('hidden');
            sectionImage.classList.add('animate-fade-in');
            sectionText.classList.add('hidden');
        }
    }

    tabTextBtn.addEventListener('click', () => switchTab('text'));
    tabImageBtn.addEventListener('click', () => switchTab('image'));

    // --- Format Selection Synchronization (Text Tab) ---
    textFormatSelect.addEventListener('change', (e) => {
        syncFormatCards(textFormatCards, e.target.value);
    });

    textFormatCards.forEach(card => {
        card.addEventListener('click', () => {
            const fmt = card.getAttribute('data-format');
            textFormatSelect.value = fmt;
            syncFormatCards(textFormatCards, fmt);
        });
    });

    // --- Format Selection Synchronization (Image Tab) ---
    imageFormatSelect.addEventListener('change', (e) => {
        syncFormatCards(imageFormatCards, e.target.value);
    });

    imageFormatCards.forEach(card => {
        card.addEventListener('click', () => {
            const fmt = card.getAttribute('data-img-format');
            imageFormatSelect.value = fmt;
            syncFormatCards(imageFormatCards, fmt);
        });
    });

    function syncFormatCards(cardsList, selectedValue) {
        cardsList.forEach(card => {
            const fmt = card.getAttribute('data-format') || card.getAttribute('data-img-format');
            if (fmt === selectedValue) {
                card.classList.add('selected');
            } else {
                card.classList.remove('selected');
            }
        });
    }

    // --- Text Input Counters ---
    function updateCounters() {
        const text = textInput.value || '';
        const chars = text.length;
        const words = text.trim() === '' ? 0 : text.trim().split(/\s+/).length;
        textCharCount.textContent = chars.toLocaleString();
        textWordCount.textContent = words.toLocaleString();
    }

    textInput.addEventListener('input', updateCounters);

    // --- Sample Text Button ---
    btnInsertSample.addEventListener('click', () => {
        textDocTitle.value = "DocMorph Architecture & Performance Overview";
        textInput.value = 
`Executive Summary
-----------------
DocMorph is a high-performance, full-stack document processing solution engineered with Spring Boot 3.3, Apache PDFBox 3.0, and Apache POI 5.3.

Core Architectural Highlights:
1. Pure In-Memory Pipeline:
All generated byte streams are processed inside memory buffers (ByteArrayOutputStream) without relying on ephemeral temporary files on the host disk. This guarantees zero file leaks and high concurrent throughput.

2. Dynamic Format Adapters:
- PDF Engine: Custom line-wrapping algorithms and page height calculators ensure seamless multi-page transitions.
- Word Engine (DOCX): Generates styled OpenXML paragraphs, headers, and embedded media with optimal EMU scaling.
- PowerPoint Engine (PPTX): Formats structured slides and automatically handles content chunking across widescreen 16:9 slides.

3. Resilience & Observability:
Comprehensive REST controllers paired with Spring's @RestControllerAdvice ensure clear, actionable JSON diagnostic error payloads whenever formatting, payload sizes, or input parameters deviate from requirements.`;
        updateCounters();
        hidePanelError();
        showToast('Sample text loaded into editor', 'info');
    });

    // --- Clear Text Button ---
    btnClearText.addEventListener('click', () => {
        textInput.value = '';
        textDocTitle.value = '';
        updateCounters();
        hidePanelError();
    });

    // --- Image Dropzone & File Handling ---
    dropzone.addEventListener('click', (e) => {
        // Prevent opening file dialog if clicking remove button
        if (e.target.closest('#btn-remove-image')) return;
        imageFileInput.click();
    });

    dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropzone.classList.add('drag-active');
    });

    dropzone.addEventListener('dragleave', () => {
        dropzone.classList.remove('drag-active');
    });

    dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropzone.classList.remove('drag-active');
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            handleImageSelection(e.dataTransfer.files[0]);
        }
    });

    imageFileInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files.length > 0) {
            handleImageSelection(e.target.files[0]);
        }
    });

    btnRemoveImage.addEventListener('click', (e) => {
        e.stopPropagation();
        clearSelectedImage();
    });

    function handleImageSelection(file) {
        if (!file.type.startsWith('image/')) {
            showPanelError(`File "${file.name}" is not a recognized image. Please select a PNG, JPG, JPEG, WEBP, or GIF.`);
            showToast('Invalid file format selected', 'error');
            clearSelectedImage();
            return;
        }

        if (file.size > 25 * 1024 * 1024) {
            showPanelError(`Image size (${formatFileSize(file.size)}) exceeds the maximum allowed limit of 25MB.`);
            showToast('Image size exceeds 25MB limit', 'error');
            clearSelectedImage();
            return;
        }

        selectedImageFile = file;
        hidePanelError();

        // Update preview UI
        previewFilename.textContent = file.name;
        previewFilesize.textContent = formatFileSize(file.size);

        const reader = new FileReader();
        reader.onload = (event) => {
            previewThumbnail.src = event.target.result;
            // Get image natural dimensions
            const img = new Image();
            img.onload = () => {
                previewDimensions.textContent = `${img.naturalWidth} × ${img.naturalHeight} px`;
            };
            img.src = event.target.result;

            dropzoneEmpty.classList.add('hidden');
            dropzonePreview.classList.remove('hidden');
            dropzonePreview.classList.add('flex');
        };
        reader.readAsDataURL(file);
    }

    function clearSelectedImage() {
        selectedImageFile = null;
        imageFileInput.value = '';
        dropzonePreview.classList.add('hidden');
        dropzonePreview.classList.remove('flex');
        dropzoneEmpty.classList.remove('hidden');
    }

    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }

    // --- Convert Text to Document Action ---
    btnConvertText.addEventListener('click', async () => {
        const text = textInput.value.trim();
        const format = textFormatSelect.value;
        const title = textDocTitle.value.trim();

        if (!text) {
            showPanelError('Please enter some text content to convert.');
            showToast('Document text content cannot be empty', 'error');
            textInput.focus();
            return;
        }

        hidePanelError();
        setTextLoading(true);

        try {
            const payload = {
                text: text,
                format: format,
                title: title || undefined
            };

            const response = await fetch('/api/convert/text', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorData = await parseResponseError(response);
                throw new Error(errorData);
            }

            // Extract file name
            const filename = extractFilename(response, `document.${format.toLowerCase()}`);
            const blob = await response.blob();

            triggerDownload(blob, filename);
            recordConversion('Text to ' + format, filename, blob.size);
            showToast(`Successfully converted and downloaded ${filename}!`, 'success');

        } catch (err) {
            console.error('Text conversion failed:', err);
            showPanelError(err.message || 'An unexpected error occurred during conversion.');
            showToast(err.message || 'Conversion failed', 'error');
        } finally {
            setTextLoading(false);
        }
    });

    // --- Convert Image to Document Action ---
    btnConvertImage.addEventListener('click', async () => {
        if (!selectedImageFile) {
            showPanelError('Please select or drop an image file first.');
            showToast('No image selected', 'error');
            return;
        }

        const format = imageFormatSelect.value;
        hidePanelError();
        setImageLoading(true);

        try {
            const formData = new FormData();
            formData.append('file', selectedImageFile);
            formData.append('format', format);

            const response = await fetch('/api/convert/image', {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                const errorData = await parseResponseError(response);
                throw new Error(errorData);
            }

            const filename = extractFilename(response, `image_doc.${format.toLowerCase()}`);
            const blob = await response.blob();

            triggerDownload(blob, filename);
            recordConversion('Image to ' + format, filename, blob.size);
            showToast(`Successfully embedded image into ${filename}!`, 'success');

        } catch (err) {
            console.error('Image conversion failed:', err);
            showPanelError(err.message || 'An error occurred while converting the image.');
            showToast(err.message || 'Conversion failed', 'error');
        } finally {
            setImageLoading(false);
        }
    });

    // --- Keyboard Shortcut: Ctrl + Enter ---
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if (activeTab === 'text') {
                btnConvertText.click();
            } else {
                btnConvertImage.click();
            }
        }
    });

    // --- Helper: Parse Response Errors ---
    async function parseResponseError(response) {
        try {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const json = await response.json();
                return json.error || json.message || `Server error (${response.status})`;
            }
            const text = await response.text();
            return text || `Conversion failed with status ${response.status}`;
        } catch {
            return `HTTP Error ${response.status}: ${response.statusText}`;
        }
    }

    // --- Helper: Extract Filename from Headers ---
    function extractFilename(response, fallback) {
        const headerName = response.headers.get('X-Document-Name');
        if (headerName) return headerName;

        const disposition = response.headers.get('Content-Disposition');
        if (disposition && disposition.indexOf('filename=') !== -1) {
            let filename = disposition.split('filename=')[1];
            if (filename.startsWith('"') && filename.endsWith('"')) {
                filename = filename.slice(1, -1);
            }
            return filename;
        }
        return fallback;
    }

    // --- Helper: Trigger Browser Download ---
    function triggerDownload(blob, filename) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        setTimeout(() => {
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        }, 300);
    }

    // --- Helper: UI Loading States ---
    function setTextLoading(isLoading) {
        btnConvertText.disabled = isLoading;
        if (isLoading) {
            btnConvertText.classList.add('opacity-75', 'cursor-not-allowed');
            btnTextLabel.textContent = 'Generating Document...';
            spinnerText.classList.remove('hidden');
        } else {
            btnConvertText.classList.remove('opacity-75', 'cursor-not-allowed');
            btnTextLabel.textContent = 'Convert & Download Document';
            spinnerText.classList.add('hidden');
        }
    }

    function setImageLoading(isLoading) {
        btnConvertImage.disabled = isLoading;
        if (isLoading) {
            btnConvertImage.classList.add('opacity-75', 'cursor-not-allowed');
            btnImageLabel.textContent = 'Embedding Image & Processing...';
            spinnerImage.classList.remove('hidden');
        } else {
            btnConvertImage.classList.remove('opacity-75', 'cursor-not-allowed');
            btnImageLabel.textContent = 'Convert Image & Download';
            spinnerImage.classList.add('hidden');
        }
    }

    // --- Helper: Error Alert Panel ---
    function showPanelError(message) {
        panelErrorMessage.textContent = message;
        panelErrorAlert.classList.remove('hidden');
    }

    function hidePanelError() {
        panelErrorAlert.classList.add('hidden');
    }

    panelErrorClose.addEventListener('click', hidePanelError);

    // --- Helper: Toast Notifications ---
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        const colors = {
            success: 'bg-emerald-950/90 border-emerald-500/50 text-emerald-200',
            error: 'bg-rose-950/90 border-rose-500/50 text-rose-200',
            info: 'bg-indigo-950/90 border-indigo-500/50 text-indigo-200'
        };

        const icons = {
            success: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />',
            error: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />',
            info: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />'
        };

        toast.className = `pointer-events-auto flex items-center space-x-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-2xl text-xs font-medium animate-fade-in ${colors[type] || colors.info}`;
        toast.innerHTML = `
            <svg class="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                ${icons[type] || icons.info}
            </svg>
            <span class="flex-grow">${message}</span>
            <button class="opacity-70 hover:opacity-100 transition ml-2">&times;</button>
        `;

        toast.querySelector('button').addEventListener('click', () => {
            toast.remove();
        });

        toastContainer.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.classList.add('opacity-0', 'transition-opacity', 'duration-300');
                setTimeout(() => toast.remove(), 300);
            }
        }, 4500);
    }

    // --- Helper: Recent Conversion Tracker ---
    function recordConversion(mode, filename, size) {
        conversionHistory.unshift({
            mode,
            filename,
            size: formatFileSize(size),
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        });

        if (conversionHistory.length > 5) {
            conversionHistory.pop();
        }

        renderRecentConversions();
    }

    function renderRecentConversions() {
        if (conversionHistory.length === 0) {
            recentConversionsCard.classList.add('hidden');
            return;
        }

        recentConversionsCard.classList.remove('hidden');
        conversionCountBadge.textContent = `${conversionHistory.length} file${conversionHistory.length > 1 ? 's' : ''}`;

        recentConversionsList.innerHTML = conversionHistory.map(item => `
            <div class="flex items-center justify-between p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 text-xs">
                <div class="flex items-center space-x-2.5 overflow-hidden">
                    <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
                    <span class="font-semibold text-slate-200 truncate">${item.filename}</span>
                    <span class="text-slate-500 font-mono-code text-[11px]">${item.size}</span>
                </div>
                <div class="flex items-center space-x-2 text-slate-400 text-[11px] flex-shrink-0 ml-2">
                    <span class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-mono-code">${item.mode}</span>
                    <span>${item.time}</span>
                </div>
            </div>
        `).join('');
    }
});
