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
            return messageDate.toLocaleDateString("id-ID", { weekday: 'long' });
        }

        return messageDate.toLocaleDateString("id-ID", {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

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

    initializeDateMarkers();


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
            appendMessage(data.text, data.ownMessage, data.timeStamp, data.product);
        });

        // Subscribe untuk status online
        stompClient.subscribe('/topic/online-users', (payload) => {
            const onlineUserIds = JSON.parse(payload.body);
            const isOnline = onlineUserIds.map(String).includes(String(receiverId));
            updateAvatarStatus(isOnline);
        });

        checkReceiverOnline();
    });

    const productPreview = document.getElementById("product-preview");
    const closePreview = document.getElementById("close-preview");

    const previewName = document.getElementById("preview-name");
    const previewPrice = document.getElementById("preview-price");
    const previewImage = document.getElementById("preview-image");

    const productModal = document.getElementById("product-modal");
    const productList = document.getElementById("product-list");
    const openProductBtn = document.getElementById("open-product-modal");

    const closeSelectModal = document.getElementById("close_modal_product")

    closeSelectModal.addEventListener("click", (e) => {
        productModal.close()
    })

    window.__products_cache = [];
    let selectedProduct = null;

    // 1️⃣ Buka modal pilih produk
    openProductBtn.addEventListener("click", () => {
        productModal.showModal();
    });

    // 2️⃣ Fetch product dari server
    async function fetchProducts() {
        try {
            const res = await fetch("/products/prew/get-all");
            if (!res.ok) throw new Error("Failed to fetch");
            const products = await res.json();
            window.__products_cache = products;
            renderProductList(products);
        } catch (e) {
            console.error("Fetch products error:", e);
        }
    }

    function renderProductList(products) {
        productList.innerHTML = "";
        products.forEach(p => {
            const item = document.createElement("div");
            item.className = "flex gap-2 items-center border border-base-300 rounded-lg p-2 hover:bg-base-200 cursor-pointer";

            const img = document.createElement("img");
            img.src = p.thumbnailUrl ? `/uploads/${p.thumbnailUrl}` : "https://via.placeholder.com/80";
            img.className = "w-14 h-14 object-cover rounded-md";

            const info = document.createElement("div");
            info.className = "flex flex-col";
            const name = document.createElement("div");
            name.className = "font-semibold text-sm";
            name.textContent = p.name;

            const price = document.createElement("div");
            price.className = "text-xs opacity-70";
            price.textContent = "Rp" + Number(p.price).toLocaleString();

            info.appendChild(name);
            info.appendChild(price);

            item.appendChild(img);
            item.appendChild(info);

            item.addEventListener("click", () => {
                selectedProduct = p;
                updateProductPreview(p);
                productModal.close();
            });

            productList.appendChild(item);
        });
    }

    function updateProductPreview(product) {
        previewName.textContent = product.name;
        previewPrice.textContent = "Rp" + Number(product.price).toLocaleString();
        previewImage.src = product.thumbnailUrl ? `/uploads/${product.thumbnailUrl}` : "https://via.placeholder.com/80";
        productPreview.classList.remove("hidden");
    }
    // 5️⃣ Close preview
    closePreview.addEventListener("click", () => {
        selectedProduct = null;
        productPreview.classList.add("hidden");
    });

    if (window.__initial_product) {
        console.log("Menginisialisasi produk awal:", window.__initial_product);
        selectedProduct = window.__initial_product;
        updateProductPreview(selectedProduct);
    }  
    fetchProducts();

    // 7️⃣ Kirim pesan
    form.addEventListener("submit", (e) => {
        e.preventDefault();

        const text = input.value.trim();
        if (!text) return;

        const payload = {
            receiverId,
            text,
            productId: selectedProduct ? selectedProduct.id : null
        };

        stompClient.send("/app/chat", {}, JSON.stringify(payload));

        input.value = "";
    });

    function appendMessage(text, isSender, timeStamp = new Date(), product = null) {
        const messageTime = new Date(timeStamp);
        const currentDateYMD = formatDateToYMD(messageTime);

        // 1. Tambah penanda tanggal jika beda
        const dateMarkers = messagesContainer.querySelectorAll('.chat-date-separator > .badge');
        const lastMarker = dateMarkers[dateMarkers.length - 1];
        let lastDateYMD = lastMarker ? lastMarker.getAttribute('data-date') : null;

        if (currentDateYMD !== lastDateYMD) {
            const dateLabel = getRelativeDateLabel(messageTime);

            const dateWrapper = document.createElement("div");
            dateWrapper.className = "flex justify-center my-2 chat-date-separator";

            const dateBadge = document.createElement("div");
            dateBadge.className = "badge badge-lg bg-base-300 text-base-content/80 shadow-md";
            dateBadge.textContent = dateLabel;
            dateBadge.setAttribute('data-date', currentDateYMD);

            dateWrapper.appendChild(dateBadge);
            messagesContainer.appendChild(dateWrapper);
        }

        // 2. Wrapper chat
        const wrapper = document.createElement("div");
        wrapper.className = `chat ${isSender ? "chat-end" : "chat-start"}`;

        const bubble = document.createElement("div");
        bubble.className = `chat-bubble shadow-lg max-w-xs md:max-w-md ${isSender ? "bg-neutral text-neutral-content" : "bg-base-200 text-base-content"}`;

        // 3. Render produk jika ada
        if (product && product.id) {
            const pWrapper = document.createElement("div");
            pWrapper.className = "mt-2 rounded-xl border border-base-300 bg-base-200 p-3 shadow-md max-w-xs md:max-w-md";

            const innerFlex = document.createElement("div");
            innerFlex.className = "flex gap-3 items-center";

            const img = document.createElement("img");
            img.src = product.thumbnailUrl ? `/uploads/${product.thumbnailUrl}` : "https://via.placeholder.com/80";
            img.className = "w-20 h-20 object-cover rounded-lg";

            const info = document.createElement("div");
            info.className = "flex flex-col";

            const name = document.createElement("div");
            name.className = "font-semibold text-base-content";
            name.textContent = product.name;

            const price = document.createElement("div");
            price.className = "text-sm opacity-100 text-success";
            price.textContent = "Rp" + Number(product.price).toLocaleString('id-ID');

            info.appendChild(name);
            info.appendChild(price);

            innerFlex.appendChild(img);
            innerFlex.appendChild(info);
            pWrapper.appendChild(innerFlex);

            bubble.appendChild(pWrapper);
        }

        // 4. Text message
        const textNode = document.createElement("div");
        textNode.textContent = text;
        bubble.appendChild(textNode);

        // 5. Footer
        const footer = document.createElement("div");
        footer.className = "chat-footer mt-1 text-xs opacity-70 text-end";
        footer.textContent = messageTime.toLocaleTimeString("id-ID", {
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