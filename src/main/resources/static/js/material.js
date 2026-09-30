(() => {
    document.querySelectorAll('[data-material-delete]').forEach((form) =>
        form.addEventListener('submit', (event) => {
            if (!window.confirm('이 자재 품목을 삭제하시겠습니까?')) event.preventDefault();
        }),
    );
    const input = document.getElementById('material-image');
    if (!input) return;
    const img = document.getElementById('preview-image');
    const placeholder = document.getElementById('image-placeholder');
    const name = document.getElementById('image-name');
    const cancel = document.getElementById('remove-image');
    const error = document.getElementById('image-error');
    const removeExisting = document.querySelector('input[type="checkbox"][name="removeImage"]');
    let url;
    const release = () => {
        if (url) URL.revokeObjectURL(url);
        url = undefined;
    };
    const resetPreview = () => {
        release();
        img.removeAttribute('src');
        img.hidden = true;
        placeholder.hidden = false;
        placeholder.textContent = '이미지를 선택하면 여기에 표시됩니다.';
        name.textContent = '';
        cancel.hidden = true;
    };
    const clear = () => {
        resetPreview();
        input.value = '';
    };
    const showPreview = () => {
        resetPreview();
        error.textContent = '';
        const file = input.files?.[0];
        if (!file) return;
        const supportedType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type);
        const unknownTypeWithImageName = !file.type && /\.(jpe?g|png|webp)$/i.test(file.name);
        if ((!supportedType && !unknownTypeWithImageName) || file.size > 10 * 1024 * 1024 || file.size === 0) {
            error.textContent = '10MB 이하의 JPG, PNG, WEBP 이미지를 선택해 주세요.';
            clear();
            return;
        }
        url = URL.createObjectURL(file);
        if (removeExisting) removeExisting.checked = false;
        placeholder.textContent = '이미지를 불러오는 중입니다…';
        name.textContent = file.name;
        cancel.hidden = false;
        img.src = url;
    };
    img.addEventListener('load', () => {
        if (!url || img.getAttribute('src') !== url) return;
        if (img.naturalWidth * img.naturalHeight > 25_000_000) {
            error.textContent = '이미지는 2,500만 화소 이하로 선택해 주세요.';
            clear();
            return;
        }
        img.hidden = false;
        placeholder.hidden = true;
        const file = input.files[0];
        name.textContent = `${file.name} · ${img.naturalWidth} × ${img.naturalHeight}`;
    });
    img.addEventListener('error', () => {
        if (!url) return;
        error.textContent = '이미지를 읽을 수 없습니다. 다른 파일을 선택해 주세요.';
        clear();
    });
    input.addEventListener('change', showPreview);
    cancel.addEventListener('click', () => {
        clear();
        error.textContent = '';
        input.dispatchEvent(new Event('input', { bubbles: true }));
    });
    removeExisting?.addEventListener('change', () => {
        if (removeExisting.checked) clear();
    });
    window.addEventListener('pagehide', release);
    window.addEventListener('pageshow', showPreview);
    showPreview();
})();
