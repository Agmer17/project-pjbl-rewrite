document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("galleryModal");
    const addBtn = document.getElementById("addGalleryButton");
    const closeBtn = document.getElementById("closeModalBtn");
    const saveBtn = document.getElementById("saveGalleryBtn");
    const container = document.getElementById("nonGalleryContainer");
    const loadingText = document.getElementById("loadingText");

    let selectedImages = [];

    addBtn.addEventListener("click", async () => {
        modal.showModal();
        container.innerHTML = "";
        loadingText.style.display = "block";

        try {
            const res = await fetch("/admin/gallery/get-non-gallery");
            const images = await res.json();
            loadingText.style.display = "none";

            if (images.length === 0) {
                container.innerHTML = `<p class="text-center text-gray-500">Semua gambar sudah ada di gallery 🎉</p>`;
                return;
            }

            images.forEach(img => {
                const card = document.createElement("div");
                card.className = "relative break-inside-avoid overflow-hidden rounded-2xl shadow group cursor-pointer";

                card.innerHTML = `
                    <img src="/uploads/${img.imageFileName}" 
                         alt="Image" 
                         class="w-full h-auto object-cover rounded-2xl transition-transform duration-300 group-hover:scale-105" />
                    <input type="checkbox" 
                           class="checkbox checkbox-error absolute top-2 right-2 scale-110 hidden" 
                           value="${img.id}" />
                `;

                const checkbox = card.querySelector("input[type='checkbox']");

                card.addEventListener("click", () => {
                    checkbox.checked = !checkbox.checked;
                    checkbox.classList.toggle("hidden", !checkbox.checked);
                    if (checkbox.checked) {
                        selectedImages.push(img.id);
                    } else {
                        selectedImages = selectedImages.filter(id => id !== img.id);
                    }
                });

                container.appendChild(card);
            });

        } catch (err) {
            console.error("Error fetching non-gallery images:", err);
            loadingText.textContent = "Gagal memuat gambar 😢";
        }
    });

    // tutup modal
    closeBtn.addEventListener("click", () => {
        modal.close();
        selectedImages = [];
    });

    // simpan perubahan
    saveBtn.addEventListener("click", async () => {
        if (selectedImages.length === 0) {
            alert("Pilih minimal satu gambar dulu!");
            return;
        }

        try {
            const res = await fetch("/admin/gallery/update-gallery", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(selectedImages)
            });

            if (res.ok) {
                modal.close();
                location.reload(); // refresh biar gallery update
            } else {
                alert("Gagal menyimpan perubahan 😢");
            }
        } catch (err) {
            console.error(err);
            alert("Terjadi kesalahan jaringan!");
        }
    });


    const deleteButtons = document.querySelectorAll('button[title="Delete image"]');
    const deleteModal = document.getElementById("deleteModal");
    const previewImg = document.getElementById("deletePreviewImg");
    const cancelBtn = document.getElementById("cancelDeleteBtn");
    const confirmBtn = document.getElementById("confirmDeleteBtn");

    let currentId = null;
    let currentCard = null;

    deleteButtons.forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            currentId = btn.getAttribute("data-id");
            currentCard = btn.closest("div.relative");
            if (!currentId || !currentCard) return;

            // tampilkan preview gambar di modal
            const imgEl = currentCard.querySelector("img");
            previewImg.src = imgEl.src;

            deleteModal.showModal();
        });
    });

    cancelBtn.addEventListener("click", () => {
        deleteModal.close();
        currentId = null;
        currentCard = null;
    });

    confirmBtn.addEventListener("click", async () => {
        if (!currentId) return;

        try {
            const res = await fetch("/admin/gallery/delete-gallery", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify([currentId])
            });

            if (res.ok) {
                if (currentCard) currentCard.remove();
                deleteModal.close();
            } else {
                alert("Gagal menghapus gambar 😢");
            }
        } catch (err) {
            console.error(err);
            alert("Terjadi kesalahan jaringan!");
        }

        currentId = null;
        currentCard = null;
    });
});