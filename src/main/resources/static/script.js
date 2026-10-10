let conversationId = null;

// Fetch agent status on load & periodic refresh
async function fetchStatus() {
    try {
        const res = await fetch('/api/status');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('agent-status-badge').textContent = data.status;
            document.getElementById('agent-status-badge').className = "px-2 py-0.5 text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full";

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

async function handleChatSubmit(event) {
    event.preventDefault();
    const inputField = document.getElementById('user-input');
    const prompt = inputField.value.trim();
    if (!prompt) return;

    inputField.value = '';
    appendMessage(prompt, 'user');

    // Show typing indicator
    const typingId = showTypingIndicator();

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt, conversationId })
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

function appendMessage(text, sender, agentName = 'BananaAgent') {
    const container = document.getElementById('chat-messages');
    const isUser = sender === 'user';

    const msgDiv = document.createElement('div');
    msgDiv.className = `flex items-start space-x-3 ${isUser ? 'flex-row-reverse space-x-reverse' : ''}`;

    const avatar = document.createElement('div');
    avatar.className = `w-8 h-8 rounded-lg flex items-center justify-center font-bold shrink-0 shadow ${
        isUser ? 'bg-indigo-600 text-white' : 'bg-gradient-to-tr from-amber-500 to-yellow-300 text-slate-950'
    }`;
    avatar.textContent = isUser ? '👤' : '🍌';

    const bubble = document.createElement('div');
    bubble.className = `max-w-xl rounded-2xl px-4 py-3 text-sm shadow-sm leading-relaxed whitespace-pre-wrap ${
        isUser
            ? 'bg-indigo-600 text-white rounded-tr-sm'
            : 'bg-slate-900 border border-slate-800 text-slate-200 rounded-tl-sm'
    }`;

    if (!isUser) {
        const nameHeader = document.createElement('p');
        nameHeader.className = 'font-semibold text-amber-400 mb-1';
        nameHeader.textContent = agentName;
        bubble.appendChild(nameHeader);
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
    avatar.className = 'w-8 h-8 rounded-lg bg-gradient-to-tr from-amber-500 to-yellow-300 flex items-center justify-center text-slate-950 font-bold shrink-0 shadow';
    avatar.textContent = '🍌';

    const bubble = document.createElement('div');
    bubble.className = 'bg-slate-900 border border-slate-800 rounded-2xl rounded-tl-sm px-4 py-3 text-sm text-slate-400 flex items-center space-x-2';
    bubble.innerHTML = `
        <span class="font-semibold text-amber-400">BananaAgent</span>
        <span class="inline-flex space-x-1">
            <span class="w-1.5 h-1.5 bg-amber-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
            <span class="w-1.5 h-1.5 bg-amber-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
            <span class="w-1.5 h-1.5 bg-amber-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
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
