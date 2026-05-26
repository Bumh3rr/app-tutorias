var COPY_ICON = '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>';
var CHECK_ICON = '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>';

function copyCredential(elementId, btn) {
    var value = document.getElementById(elementId).textContent.trim();
    function markCopied() {
        btn.innerHTML = CHECK_ICON;
        btn.classList.add('copied');
        setTimeout(function () {
            btn.innerHTML = COPY_ICON;
            btn.classList.remove('copied');
        }, 1500);
    }
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(value).then(markCopied).catch(function () {
            fallbackCopy(value);
            markCopied();
        });
    } else {
        fallbackCopy(value);
        markCopied();
    }
}

function fallbackCopy(text) {
    var ta = document.createElement('textarea');
    ta.value = text;
    ta.style.cssText = 'position:fixed;opacity:0;pointer-events:none;';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    try { document.execCommand('copy'); } catch (e) {}
    document.body.removeChild(ta);
}

function dismissCredentialsCard() {
    var card = document.getElementById('credentialsCard');
    if (!card) return;
    card.classList.add('credentials-card--dismissing');
    setTimeout(function () { card.remove(); }, 380);
}
