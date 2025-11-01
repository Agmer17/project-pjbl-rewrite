"use strict";

// ✅ Ubah struktur data sesuai DTO baru
let updatedImageIds = [];      // List<UUID>
let updatedImageFiles = [];    // List<MultipartFile>
let imageToDelete = new Set(); // Set<UUID>
let newImagesFilesTemp = new Map();

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

    // ===== HANDLE ADD NEW IMAGE =====
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

            console.log(newImagesFilesTemp.values())
            imgEl.dataset.index = fIndex


            imgEl.classList.remove("hidden");
            // 💡 REVISI: Menggunakan h-full object-cover untuk aspek kotak dan terpusat
            imgEl.classList.add(
                "w-full",
                "h-full",
                "object-cover", // PAKSA CENTER & FILL
                "transition-transform",
                "duration-300",
                "group-hover:scale-105"
            );

            if (svgPlaceholder) svgPlaceholder.classList.add("hidden");
            if (textPlaceholder) textPlaceholder.classList.add("hidden");

            // 💡 REVISI: Mengubah class wrapper agar sesuai dengan style gambar yang sudah ada (kotak)
            wrapper.className = "relative w-full rounded-lg overflow-hidden group shadow-md shadow-base-300/70 border border-base-300/70 aspect-square";

            const overlay = wrapper.querySelector(".overlay");
            const actions = wrapper.querySelector(".actions");
            if (overlay) overlay.classList.remove("hidden");
            if (actions) actions.classList.remove("hidden");

            console.log(newImagesFilesTemp)

            imgEl.onload = () => URL.revokeObjectURL(imgEl.src)



        });
    });

    // ===== HANDLE DELETE NEW IMAGE =====
    document.querySelectorAll(".btn-new-delete").forEach(delBtn => {
        delBtn.addEventListener("click", (e) => {
            const actionsDiv = e.target.closest(".actions");
            if (!actionsDiv) return;

            const wrapper = actionsDiv.closest(".relative");
            if (!wrapper) return;

            const img = wrapper.querySelector(".new-img-uploads");
            const dataIndex = img?.dataset.index;

            // Logika Map tetap sama
            if (dataIndex && newImagesFilesTemp.has(dataIndex)) {
                newImagesFilesTemp.delete(dataIndex);
            }

            resetImageSlot(wrapper);

            console.log(newImagesFilesTemp)


        });
    });

    // ===== HANDLE EDIT NEW IMAGE SLOT (RE-UPLOAD) =====
    document.querySelectorAll(".input-new-edit").forEach(update => {
        update.addEventListener("change", (e) => {

            const file = e.target.files[0];
            if (!file) return;

            const parent = e.target.closest("label");

            const wrapper = parent.closest(".relative");

            const imgEl = wrapper.querySelector(".new-img-uploads");

            imgEl.src = URL.createObjectURL(file);


            const index = imgEl.dataset.index;

            // Logika Map tetap sama
            newImagesFilesTemp.set(index, file)

            console.log(newImagesFilesTemp)
            imgEl.onload = () => URL.revokeObjectURL(imgEl.src)

            console.log(newImagesFilesTemp)

        })
    })

    // ===== PRICE INPUT FORMATTING (Tetap sama) =====
    const input = document.querySelector("#price");
    input.addEventListener("input", (e) => {
        const cursor = input.selectionStart; // simpan posisi kursor
        const rawValue = input.value.replace(/[^\d]/g, ""); // hapus semua non-digit
        const formatted = new Intl.NumberFormat("id-ID").format(rawValue || 0);
        input.value = formatted;
        input.setSelectionRange(cursor, cursor); // biar kursor gak lompat
    });

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

    // ===== FORM SUBMIT LOGIC (Tetap sama) =====
    const form = document.querySelector("#updateProductForm");

    form.addEventListener("submit", (e) => {
        // jangan preventDefault, biar SSR redirect + flash attribute jalan

        // --- Bersihkan hidden/input file dinamis lama ---
        form.querySelectorAll("input[data-dynamic]").forEach(el => el.remove());

        // --- Price hidden (bersih tanpa titik ribuan) ---
        let rawPrice = priceInput.value.replace(/\./g, "");
        let priceHidden = document.createElement("input");
        priceHidden.type = "hidden";
        priceHidden.name = "price"; // ini yang akan dikirim ke Spring
        priceHidden.value = rawPrice;
        priceHidden.dataset.dynamic = "true";
        form.appendChild(priceHidden);

        // --- File baru ---
        newImagesFilesTemp.forEach(file => {
            const dt = new DataTransfer();
            dt.items.add(file);
            const input = document.createElement("input");
            input.type = "file";
            input.name = "newImagesFiles";
            input.files = dt.files;
            input.dataset.dynamic = "true";
            form.appendChild(input);
        });

        // --- File yang diupdate ---
        updatedImageFiles.forEach(file => {
            const dt = new DataTransfer();
            dt.items.add(file);
            const input = document.createElement("input");
            input.type = "file";
            input.name = "updatedImageFiles";
            input.files = dt.files;
            input.dataset.dynamic = "true";
            form.appendChild(input);
        });

        // --- ID gambar yang diupdate ---
        updatedImageIds.forEach(id => {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = "updatedImageIds";
            input.value = id;
            input.dataset.dynamic = "true";
            form.appendChild(input);
        });

        // --- ID gambar yang dihapus ---
        imageToDelete.forEach(id => {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = "imageToDelete";
            input.value = id;
            input.dataset.dynamic = "true";
            form.appendChild(input);
        });

        // Debug
        console.log("=== DATA YANG DIKIRIM ===");
        console.log("Price:", rawPrice);
        console.log("New Files:", [...newImagesFilesTemp.values()].map(f => f.name));
        console.log("Updated Files:", [...updatedImageFiles.values()].map(f => f.name));
        console.log("Updated IDs:", [...updatedImageIds]);
        console.log("Deleted IDs:", [...imageToDelete]);

        // --- Submit form ---
        form.submit();
    });



})

// ===== RESET IMAGE SLOT FUNCTION (Hanya Perbaikan Class) =====
function resetImageSlot(wrapper) {
    const imgEl = wrapper.querySelector(".new-img-uploads");

    const svgPlaceholder = wrapper.querySelector(".placeholder-svg");
    const textPlaceholder = wrapper.querySelector("label span");

    const overlay = wrapper.querySelector(".overlay");
    const actions = wrapper.querySelector(".actions");

    if (imgEl) {
        imgEl.classList.add("hidden");
        imgEl.removeAttribute("src");
        delete imgEl.dataset.index;
        // 💡 REVISI: Menghapus class h-full w-full object-cover
        imgEl.classList.remove("w-full", "h-full", "object-cover", "transition-transform", "duration-300", "group-hover:scale-105");
    }

    if (svgPlaceholder) svgPlaceholder.classList.remove("hidden");
    if (textPlaceholder) textPlaceholder.classList.remove("hidden");

    if (overlay) overlay.classList.add("hidden");
    if (actions) actions.classList.add("hidden");

    // 💡 REVISI: Mengembalikan class wrapper ke kondisi awal yang memiliki aspect-square
    wrapper.className =
        "relative w-full aspect-square flex items-center justify-center border-2 border-dashed border-primary/50 rounded-lg cursor-pointer hover:bg-base-300 transition-colors duration-300 group shadow-md shadow-base-300/70";
}