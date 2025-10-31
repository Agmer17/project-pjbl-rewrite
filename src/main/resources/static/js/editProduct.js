"use strict";

// ✅ Ubah struktur data sesuai DTO baru
let updatedImageIds = [];      // List<UUID>
let updatedImageFiles = [];    // List<MultipartFile>
let imageToDelete = new Set(); // Set<UUID>
let newImagesFilesTemp = [];   // List<File>

document.addEventListener("DOMContentLoaded", () => {

    // ===== HANDLE EDIT EXISTING IMAGE =====
    document.querySelectorAll(".input-image-edit").forEach(input => {
        input.addEventListener("change", (e) => {
            const newImage = e.target.files[0];
            if (!newImage) return;

            const imageId = e.target.dataset.imageId;
            const container = e.target.closest(".relative");
            const imagePreview = container.querySelector("img");

            // Preview image baru
            imagePreview.src = URL.createObjectURL(newImage);

            // ✅ Cek apakah ID ini sudah ada di updatedImageIds
            const existingIndex = updatedImageIds.indexOf(imageId);

            if (existingIndex !== -1) {
                // Sudah ada, replace file-nya aja
                updatedImageFiles[existingIndex] = newImage;
            } else {
                // Belum ada, tambah baru
                updatedImageIds.push(imageId);
                updatedImageFiles.push(newImage);
            }

            // Hapus dari delete list (kalau ada)
            imageToDelete.delete(imageId);

            console.log("📝 Updated IDs:", updatedImageIds);
            console.log("📝 Updated Files:", updatedImageFiles.map(f => f.name));
            console.log("🗑️ To Delete:", Array.from(imageToDelete));
        });
    });

    // ===== HANDLE DELETE EXISTING IMAGE =====
    document.querySelectorAll(".btn-delete").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const button = e.target.closest(".btn-delete");
            const imageId = button.dataset.imageId;
            const container = button.closest(".relative");
            const imagePreview = container.querySelector("img");

            // Set placeholder image
            imagePreview.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";

            // ✅ Hapus dari updated list (kalau ada)
            const existingIndex = updatedImageIds.indexOf(imageId);
            if (existingIndex !== -1) {
                updatedImageIds.splice(existingIndex, 1);
                updatedImageFiles.splice(existingIndex, 1);
            }

            // ✅ Tambah ke delete list
            imageToDelete.add(imageId);

            console.log("🗑️ To Delete:", Array.from(imageToDelete));
            console.log("📝 Updated IDs:", updatedImageIds);
        });
    });

    document.querySelectorAll(".input-image-new").forEach(input => {
        input.addEventListener("change", e => {
            const file = e.target.files[0];
            if (!file) return;

            const parent = e.target.closest("label");

            const wrapper = parent.closest(".relative");

            const imgEl = wrapper.querySelector(".new-img-uploads");
            const svgPlaceholder = parent.querySelector("svg");

            const textPlaceholder = parent.querySelector("span");

            imgEl.src = URL.createObjectURL(file);


            const fIndex = String(Date.now())
            newImagesFilesTemp.set(fIndex, file)
            imgEl.dataset.index = fIndex


            imgEl.classList.remove("hidden");
            imgEl.classList.add(
                "w-full",
                "h-56",
                "md:h-64",
                "object-cover",
                "transition-transform",
                "duration-300",
                "group-hover:scale-105"
            );

            if (svgPlaceholder) svgPlaceholder.classList.add("hidden");
            if (textPlaceholder) textPlaceholder.classList.add("hidden");

            wrapper.className = "relative w-full rounded-2xl overflow-hidden group shadow-lg border border-neutral";

            const overlay = wrapper.querySelector(".overlay");
            const actions = wrapper.querySelector(".actions");
            if (overlay) overlay.classList.remove("hidden");
            if (actions) actions.classList.remove("hidden");

            console.log(newImagesFilesTemp)

            imgEl.onload = () => URL.revokeObjectURL(imgEl.src)



        });
    });

    document.querySelectorAll(".btn-new-delete").forEach(delBtn => {
        delBtn.addEventListener("click", (e) => {
            const actionsDiv = e.target.closest(".actions");
            if (!actionsDiv) return;

            const wrapper = actionsDiv.closest(".relative");
            if (!wrapper) return;

            const img = wrapper.querySelector(".new-img-uploads");
            const dataIndex = img?.dataset.index;

            const svgPlaceholder = wrapper.querySelector(".placeholder-svg");
            if (svgPlaceholder) svgPlaceholder.classList.remove("hidden");

            if (dataIndex && newImagesFilesTemp.has(dataIndex)) {
                newImagesFilesTemp.delete(dataIndex);
            }

            resetImageSlot(wrapper);

            console.log(newImagesFilesTemp)


        });
    });

    document.querySelectorAll(".input-new-edit").forEach(update => {
        update.addEventListener("change", (e) => {

            const file = e.target.files[0];
            if (!file) return;

            const parent = e.target.closest("label");

            const wrapper = parent.closest(".relative");

            const imgEl = wrapper.querySelector(".new-img-uploads");

            imgEl.src = URL.createObjectURL(file);


            const index = imgEl.dataset.index;

            newImagesFilesTemp.set(index, file)
            imgEl.onload = () => URL.revokeObjectURL(imgEl.src)

            console.log(newImagesFilesTemp)

        })
    })


    const input = document.querySelector("#price");
    input.addEventListener("input", (e) => {
        const cursor = input.selectionStart; // simpan posisi kursor
        const rawValue = input.value.replace(/[^\d]/g, ""); // hapus semua non-digit
        const formatted = new Intl.NumberFormat("id-ID").format(rawValue || 0);
        input.value = formatted;
        input.setSelectionRange(cursor, cursor); // biar kursor gak lompat
    });


    const form = document.querySelector("#updateProductForm");
    const priceInput = document.querySelector("#price");

    // bikin supaya saat diketik ada format titik ribuan
    priceInput.addEventListener("input", (e) => {
        let value = e.target.value.replace(/\./g, "");
        if (!isNaN(value) && value.length > 0) {
            e.target.value = Number(value).toLocaleString("id-ID");
        } else {
            e.target.value = "";
        }
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const formData = new FormData();
        const id = document.querySelector("#productId")?.value;
        const name = document.querySelector("#productName")?.value;
        const description = document.querySelector("#productDescription")?.value;
        const price = priceInput.value.replace(/\./g, "");
        const categoryId = document.querySelector("select[name='categoryId']")?.value;

        // ✅ Basic fields
        formData.append("id", id);
        formData.append("name", name);
        formData.append("description", description);
        formData.append("price", price);
        formData.append("categoryId", categoryId);

        // ✅ New images (List<MultipartFile>)
        newImagesFilesTemp.forEach((file) => {
            formData.append("newImagesFiles", file);
        });

        // ✅ Images to delete (List<UUID>)
        imageToDelete.forEach((id) => {
            formData.append("imageToDelete", id);
        });

        // ✅ Updated images - 2 list terpisah (harus urutan sama!)
        updatedImageIds.forEach((id) => {
            formData.append("updatedImageIds", id);
        });

        updatedImageFiles.forEach((file) => {
            formData.append("updatedImageFiles", file);
        });

        // Debug log
        console.log("=== SUBMITTING DATA ===");
        console.log("Updated IDs:", updatedImageIds);
        console.log("Updated Files:", updatedImageFiles.map(f => f.name));
        console.log("New Images:", newImagesFilesTemp.map(f => f.name));
        console.log("To Delete:", Array.from(imageToDelete));

        try {
            const res = await fetch(`/admin/products/edit/${id}`, {
                method: "POST",
                body: formData
            });

            if (res.redirected) {
                window.location.href = res.url;
            } else if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
        } catch (err) {
            console.error("❌ Gagal mengirim data:", err);
            alert("Terjadi kesalahan saat menyimpan produk. Silakan coba lagi.");
        }
    });

})

function resetImageSlot(wrapper) {
    const imgEl = wrapper.querySelector(".new-img-uploads");

    // Perbaikan: Gunakan class unik .placeholder-svg untuk menargetkan SVG placeholder utama
    const svgPlaceholder = wrapper.querySelector(".placeholder-svg");

    // Asumsi: span "Tambah gambar" berada dalam label yang sama dengan placeholder-svg
    const textPlaceholder = wrapper.querySelector("label span");

    const overlay = wrapper.querySelector(".overlay");
    const actions = wrapper.querySelector(".actions");

    if (imgEl) {
        imgEl.classList.add("hidden");
        imgEl.removeAttribute("src");
        delete imgEl.dataset.index;
    }

    // SVG placeholder sekarang akan muncul kembali
    if (svgPlaceholder) svgPlaceholder.classList.remove("hidden");

    // Text placeholder juga muncul kembali
    if (textPlaceholder) textPlaceholder.classList.remove("hidden");

    if (overlay) overlay.classList.add("hidden");
    if (actions) actions.classList.add("hidden");

    wrapper.className =
        "relative w-full h-56 md:h-64 flex items-center justify-center border-2 border-dashed border-neutral-400 rounded-2xl cursor-pointer hover:bg-neutral/10 transition-colors duration-300 group";
}
