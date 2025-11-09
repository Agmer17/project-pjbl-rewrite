window.addEventListener("DOMContentLoaded", () => {
    // === 1. PENGAMBILAN ID PENGGUNA ===
    const userIdEl = document.querySelector("#userId");
    const receiverIdEl = document.querySelector("#receiverId");

    let userId = null;
    let receiverId = null;

    if (userIdEl && receiverIdEl) {
        // Ambil dari atribut data-userId / data-receiverId
        userId = userIdEl.getAttribute('data-userId');
        receiverId = receiverIdEl.getAttribute('data-receiverId');

        if (!userId) {
            console.error("❌ data-userId kosong!");
            return;
        }
        console.log("✅ Current User ID:", userId);
    } else {
        console.error("❌ Element #userId atau #receiverId tidak ditemukan!");
        return;
    }

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    const receiverAvatar = document.getElementById('receiverAvatar');
    const form = document.getElementById("chat-form");
    const input = document.getElementById("chat-input");
    const messagesContainer = document.getElementById("chat-messages");

    // Scroll ke bawah saat pertama kali dimuat
    messagesContainer.scrollTop = messagesContainer.scrollHeight;


    // === 2. UTILITAS TANGGAL ===
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Atur ke tengah malam hari ini

    function formatDateToYMD(date) {
        // Fungsi untuk mengonversi Date object ke string YYYY-MM-DD
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    function getRelativeDateLabel(timestamp) {
        // Fungsi untuk mendapatkan label relatif (Hari Ini, Kemarin, Nama Hari, atau Tanggal Penuh)
        const messageDate = new Date(timestamp);
        const messageDateOnly = new Date(messageDate);
        messageDateOnly.setHours(0, 0, 0, 0);

        const diffTime = today.getTime() - messageDateOnly.getTime();
        // Hitung selisih hari (menggunakan pembulatan untuk memastikan hari yang tepat)
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return "Hari Ini";
        if (diffDays === 1) return "Kemarin";
        if (diffDays < 7) {
            // Kurang dari seminggu: tampilkan nama hari
            return messageDate.toLocaleDateString("id-ID", { weekday: 'long' });
        }

        // Lebih dari atau sama dengan seminggu: tampilkan tanggal penuh
        return messageDate.toLocaleDateString("id-ID", {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    // === 3. INISIALISASI (Mengubah Label Hari Thymeleaf ke Label Relatif) ===
    function initializeDateMarkers() {
        // Mencari elemen pemisah hari yang dibuat oleh Thymeleaf
        const markers = messagesContainer.querySelectorAll('.chat-date-separator > .badge');
        markers.forEach(badge => {
            const dateString = badge.getAttribute('data-date'); // YYYY-MM-DD
            if (dateString) {
                // Buat Date object dari string YYYY-MM-DD
                const parts = dateString.split('-');
                // Catatan: Month di JS 0-indexed, jadi parts[1]-1
                const dateObj = new Date(parts[0], parts[1] - 1, parts[2]);

                // Ganti teks tanggal penuhnya dengan label relatif ("Hari Ini", "Kemarin", dll.)
                badge.textContent = getRelativeDateLabel(dateObj);
            }
        });
    }

    // Panggil saat DOMContentLoaded
    initializeDateMarkers();


    // === 4. FUNGSI CHAT DAN WEBSOCKET ===

    // ... (Fungsi checkReceiverOnline dan updateAvatarStatus tetap sama) ...
    async function checkReceiverOnline() {
        try {
            const res = await fetch('/live-chat/online-users');
            if (!res.ok) throw new Error('Fetch failed');
            const onlineUserIds = await res.json();
            const isOnline = onlineUserIds.map(String).includes(String(receiverId));
            updateAvatarStatus(isOnline);
        } catch (err) {
            console.error('❌ Gagal ambil online users:', err);
        }
    }

    function updateAvatarStatus(isOnline) {
        receiverAvatar.classList.remove('avatar-online', 'avatar-offline');
        receiverAvatar.classList.add(isOnline ? 'avatar-online' : 'avatar-offline');

        const ringElement = receiverAvatar.querySelector('.rounded-full');
        if (ringElement) {
            ringElement.classList.remove('ring-base-200', 'ring-success');
            ringElement.classList.add(isOnline ? 'ring-success' : 'ring-base-200');
        }
    }


    stompClient.connect({}, () => {
        console.log('✅ WebSocket Connected');

        // Subscribe untuk pesan masuk
        stompClient.subscribe(`/user/${userId}/queue/messages`, (msg) => {
            const data = JSON.parse(msg.body);
            // Gunakan data.timeStamp (epoch/string dari server)
            appendMessage(data.text, data.ownMessage, data.timeStamp);
        });

        // Subscribe untuk status online
        stompClient.subscribe('/topic/online-users', (payload) => {
            const onlineUserIds = JSON.parse(payload.body);
            const isOnline = onlineUserIds.map(String).includes(String(receiverId));
            updateAvatarStatus(isOnline);
        });

        checkReceiverOnline(); // Cek status online pertama kali
    });

    // === ✉️ Kirim pesan ===
    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;

        const payload = { receiverId, text };
        stompClient.send("/app/chat", {}, JSON.stringify(payload));
        input.value = "";
    });

    // === 💬 Tambah pesan ke UI (Mendukung Pemisah Hari) ===
    function appendMessage(text, isSender, timeStamp = new Date()) {
        const messageTime = new Date(timeStamp);
        const currentDateYMD = formatDateToYMD(messageTime);

        // 1. Cek tanggal dari penanda hari terakhir yang ada di UI
        const dateMarkers = messagesContainer.querySelectorAll('.chat-date-separator > .badge');
        const lastMarker = dateMarkers[dateMarkers.length - 1];
        let lastDateYMD = lastMarker ? lastMarker.getAttribute('data-date') : null;

        // 2. Tambahkan penanda hari jika tanggalnya berbeda
        if (currentDateYMD !== lastDateYMD) {
            const dateLabel = getRelativeDateLabel(messageTime);

            const dateWrapper = document.createElement("div");
            dateWrapper.className = "flex justify-center my-2 chat-date-separator"; // Tambah class separator

            const dateBadge = document.createElement("div");
            dateBadge.className = "badge badge-lg bg-base-300 text-base-content/80 shadow-md";
            dateBadge.textContent = dateLabel;
            dateBadge.setAttribute('data-date', currentDateYMD); // Simpan YMD untuk perbandingan berikutnya

            dateWrapper.appendChild(dateBadge);
            messagesContainer.appendChild(dateWrapper);
        }

        // 3. Tambahkan elemen pesan seperti biasa
        const wrapper = document.createElement("div");
        wrapper.className = `chat ${isSender ? "chat-end" : "chat-start"}`;

        const bubble = document.createElement("div");
        bubble.className = `chat-bubble shadow-lg max-w-xs md:max-w-md ${isSender ? "bg-neutral text-neutral-content" : "bg-base-200 text-base-content"}`;
        bubble.textContent = text;

        const footer = document.createElement("div");
        footer.className = "chat-footer mt-1 text-xs opacity-70";
        // Asumsi timeStamp di sini adalah Date object atau epoch millisecond
        footer.textContent = new Date(messageTime).toLocaleTimeString("id-ID", {
            hour: "2-digit",
            minute: "2-digit"
        });

        wrapper.appendChild(bubble);
        wrapper.appendChild(footer);
        messagesContainer.appendChild(wrapper);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    window.addEventListener('beforeunload', () => {
        if (stompClient.connected) stompClient.disconnect();
    });

})