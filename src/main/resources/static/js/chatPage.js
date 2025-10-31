document.addEventListener("DOMContentLoaded", () => {
    const userIdEl = document.querySelector("#userId");
    let currentUserId = null;

    if (userIdEl) {
        // 🔥 PAKAI getAttribute supaya gak ribet sama case-nya
        currentUserId = userIdEl.getAttribute('data-userId');

        if (!currentUserId) {
            console.error("❌ data-userId kosong!");
            return;
        }
        console.log("✅ Current User ID:", currentUserId);
    } else {
        console.error("❌ Element #userId tidak ditemukan!");
        return;
    }

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    const chatListContainer = document.getElementById('chatListContainer');
    let onlineUsersSet = new Set();

    async function fetchOnlineUsers() {
        try {
            const res = await fetch('/live-chat/online-users');
            if (!res.ok) throw new Error('Failed to fetch online users');
            const onlineUserIds = await res.json();
            console.log("🟢 Online users:", onlineUserIds);
            onlineUsersSet = new Set(onlineUserIds.map(id => String(id)));
            updateAllAvatars();
        } catch (err) {
            console.error("❌ Gagal fetch online users:", err);
        }
    }

    function updateAllAvatars() {
        document.querySelectorAll('.chat-item').forEach(item => {
            const userId = item.dataset.id;
            const avatar = item.querySelector('.avatar > div');
            if (avatar) {
                if (onlineUsersSet.has(userId)) {
                    avatar.classList.remove('border-base-300');
                    avatar.classList.add('border-success');
                    avatar.parentElement.classList.add('avatar-online');
                    avatar.parentElement.classList.remove('avatar-offline');
                } else {
                    avatar.classList.remove('border-success');
                    avatar.classList.add('border-base-300');
                    avatar.parentElement.classList.add('avatar-offline');
                    avatar.parentElement.classList.remove('avatar-online');
                }
            }
        });
    }

    stompClient.connect({}, () => {
        console.log('✅ Connected to WebSocket');
        console.log('📡 Subscribing to:', `/user/${currentUserId}/queue/messages`);

        stompClient.subscribe(`/user/${currentUserId}/queue/messages`, (payload) => {
            console.log("📨 Message received:", payload.body);
            const message = JSON.parse(payload.body);
            updateChatList(message);
        });

        stompClient.subscribe('/topic/presence', (payload) => {
            const event = JSON.parse(payload.body);
            handlePresenceEvent(event);
        });

        stompClient.subscribe('/topic/online-users', (payload) => {
            const onlineUserIds = JSON.parse(payload.body);
            console.log("🔄 Online users updated:", onlineUserIds);
            onlineUsersSet = new Set(onlineUserIds.map(id => String(id)));
            updateAllAvatars();
        });

        fetchOnlineUsers();
    }, (error) => {
        console.error('❌ WebSocket connection error:', error);
    });

    function handlePresenceEvent(event) {
        const { userId, type } = event;
        if (type === 'USER_ONLINE') {
            onlineUsersSet.add(String(userId));
        } else if (type === 'USER_OFFLINE') {
            onlineUsersSet.delete(String(userId));
        }
        updateAllAvatars();
    }

    function updateChatList(message) {
        console.log("🔄 Updating chat list with:", message);
        const { sender, text, timestamp } = message;

        const chatItem = document.querySelector(`.chat-item[data-id="${sender}"]`);

        if (chatItem) {
            const preview = chatItem.querySelector('.chat-preview');
            const timeElement = chatItem.querySelector('.chat-time');

            if (preview) {
                preview.textContent = text;
                preview.classList.remove('italic', 'text-neutral-400');
            }

            if (timeElement) {
                const msgTime = timestamp ? new Date(timestamp) : new Date();
                timeElement.textContent = msgTime.toLocaleTimeString('id-ID', {
                    hour: '2-digit',
                    minute: '2-digit'
                });
            }

            // 🟡 Tambah penanda pesan baru
            let badge = chatItem.querySelector('.new-msg-badge');
            if (!badge) {
                badge = document.createElement('span');
                badge.className = 'new-msg-badge badge badge-success badge-sm ml-2 animate-bounce';
                badge.textContent = 'Baru';
                const rightSection = chatItem.querySelector('.chat-meta') || chatItem;
                rightSection.appendChild(badge);
            }

            // Pindahkan ke paling atas
            chatListContainer.prepend(chatItem);
            console.log(`✅ Chat list updated for sender: ${sender}`);
        } else {
            console.warn(`⚠️ Chat item not found for sender: ${sender}`);
        }
    }

    document.addEventListener('click', (e) => {
        const chatItem = e.target.closest('.chat-item');
        if (chatItem) {
            const badge = chatItem.querySelector('.new-msg-badge');
            if (badge) {
                badge.remove();
            }
        }
    });

    window.addEventListener('beforeunload', () => {
        if (stompClient.connected) {
            stompClient.disconnect();
        }
    });
});