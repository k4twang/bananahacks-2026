let conversationId = null;
let selectedImageData = null;

// Fetch agent status on load & periodic refresh
async function fetchStatus() {
    try {
        const res = await fetch('/api/status');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('agent-status-badge').textContent = data.status;
            document.getElementById('agent-status-badge').className = "px-2 py-0.5 text-xs font-medium bg-yellow-500/10 text-yellow-300 border border-yellow-500/30 rounded-full";

            const uptimeSec = Math.floor(data.uptimeMs / 1000);
            document.getElementById('uptime-display').textContent = `${uptimeSec}s`;
            document.getElementById('tools-display').textContent = data.activeTools.join(', ');
        }
    } catch (e) {
        console.error('Failed to fetch status:', e);
        document.getElementById('agent-status-badge').textContent = 'OFFLINE';
        document.getElementById('agent-status-badge').className = "px-2 py-0.5 text-xs font-medium bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded-full";
    }
}

setInterval(fetchStatus, 3000);
fetchStatus();

function handleImageSelected(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        selectedImageData = e.target.result;
        document.getElementById('image-preview').src = selectedImageData;
        document.getElementById('image-name').textContent = file.name;
        document.getElementById('image-preview-container').classList.remove('hidden');
    };
    reader.readAsDataURL(file);
}

function clearImage() {
    selectedImageData = null;
    document.getElementById('image-file').value = '';
    document.getElementById('image-preview-container').classList.add('hidden');
}

async function handleChatSubmit(event) {
    event.preventDefault();
    const inputField = document.getElementById('user-input');
    const prompt = inputField.value.trim();
    if (!prompt && !selectedImageData) return;

    const messageText = prompt || "Please analyze this image.";
    const imageUrl = selectedImageData;

    inputField.value = '';
    clearImage();

    appendMessage(messageText, 'user', 'You', imageUrl);

    // Show typing indicator
    const typingId = showTypingIndicator();

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt: messageText, conversationId, imageUrl })
        });

        removeTypingIndicator(typingId);

        if (response.ok) {
            const data = await response.json();
            conversationId = data.conversationId;
            appendMessage(data.response, 'agent', data.agentName);
        } else {
            appendMessage('Error: Server returned status ' + response.status, 'agent', 'System');
        }
    } catch (err) {
        removeTypingIndicator(typingId);
        appendMessage('Network error: Could not reach agent server.', 'agent', 'System');
    }
}

function sendPresetPrompt(text) {
    document.getElementById('user-input').value = text;
    document.getElementById('chat-form').dispatchEvent(new Event('submit', { cancelable: true }));
}

function appendMessage(text, sender, agentName = 'Second Life', imageUrl = null) {
    const container = document.getElementById('chat-messages');
    const isUser = sender === 'user';

    const msgDiv = document.createElement('div');
    msgDiv.className = `flex items-start space-x-3 ${isUser ? 'flex-row-reverse space-x-reverse' : ''}`;

    const avatar = document.createElement('div');
    avatar.className = `w-8 h-8 rounded-lg flex items-center justify-center font-bold shrink-0 shadow ${
        isUser ? 'bg-yellow-400 text-zinc-950 font-extrabold' : 'bg-gradient-to-tr from-yellow-400 to-amber-200 text-zinc-950'
    }`;
    avatar.textContent = isUser ? '👤' : '⚡';

    const bubble = document.createElement('div');
    bubble.className = `max-w-xl rounded-2xl px-4 py-3 text-sm shadow-sm leading-relaxed whitespace-pre-wrap ${
        isUser
            ? 'bg-yellow-400 text-zinc-950 font-medium rounded-tr-sm'
            : 'bg-zinc-900 border border-zinc-800 text-zinc-100 rounded-tl-sm'
    }`;

    if (!isUser) {
        const nameHeader = document.createElement('p');
        nameHeader.className = 'font-semibold text-yellow-400 mb-1';
        nameHeader.textContent = agentName;
        bubble.appendChild(nameHeader);
    }

    if (imageUrl) {
        const img = document.createElement('img');
        img.src = imageUrl;
        img.className = 'w-full max-h-60 object-cover rounded-xl mb-2 border border-yellow-500/30';
        bubble.appendChild(img);
    }

    const textNode = document.createElement('div');
    textNode.textContent = text;
    bubble.appendChild(textNode);

    msgDiv.appendChild(avatar);
    msgDiv.appendChild(bubble);
    container.appendChild(msgDiv);

    container.scrollTop = container.scrollHeight;
}

function showTypingIndicator() {
    const container = document.getElementById('chat-messages');
    const id = 'typing-' + Date.now();

    const msgDiv = document.createElement('div');
    msgDiv.id = id;
    msgDiv.className = 'flex items-start space-x-3';

    const avatar = document.createElement('div');
    avatar.className = 'w-8 h-8 rounded-lg bg-gradient-to-tr from-yellow-400 to-amber-200 flex items-center justify-center text-zinc-950 font-bold shrink-0 shadow';
    avatar.textContent = '⚡';

    const bubble = document.createElement('div');
    bubble.className = 'bg-zinc-900 border border-zinc-800 rounded-2xl rounded-tl-sm px-4 py-3 text-sm text-zinc-400 flex items-center space-x-2';
    bubble.innerHTML = `
        <span class="font-semibold text-yellow-400">Second Life</span>
        <span class="inline-flex space-x-1">
            <span class="w-1.5 h-1.5 bg-yellow-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
            <span class="w-1.5 h-1.5 bg-yellow-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
            <span class="w-1.5 h-1.5 bg-yellow-400 rounded-ful animate-bounce" style="animation-delay: 300ms"></span>
        </span>
    `;

    msgDiv.appendChild(avatar);
    msgDiv.appendChild(bubble);
    container.appendChild(msgDiv);
    container.scrollTop = container.scrollHeight;

    return id;
}

function removeTypingIndicator(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
}
