document.addEventListener("DOMContentLoaded", () => {
    const mainInput = document.getElementById("mainImageInput");
    const mainPreview = document.getElementById("mainPreview");
    const priceInput = document.getElementById("priceInput");
    const form = document.querySelector("form"); // Ambil form element

    mainInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (ev) => mainPreview.src = ev.target.result;
            reader.readAsDataURL(file);
        }
    });

    // Gambar tambahan (maks 4)
    for (let i = 1; i <= 4; i++) {
        const input = document.getElementById(`extraImage${i}`);
        const preview = document.getElementById(`extraPreview${i}`);
        if (!input) continue;
        input.addEventListener("change", (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (ev) => preview.src = ev.target.result;
                reader.readAsDataURL(file);
            }
        });
    }

    // Format harga
    priceInput.addEventListener("input", (e) => {
        let value = e.target.value.replace(/,/g, "");
        if (!isNaN(value) && value.length > 0) {
            e.target.value = Number(value).toLocaleString("en-US");
        } else {
            e.target.value = "";
        }
    });

    // 🔥 INTERCEPT FORM SUBMIT - Kirim hanya file yang valid
    form?.addEventListener("submit", async (e) => {
        e.preventDefault(); // Stop default submit

        // Bersihkan format harga
        priceInput.value = priceInput.value.replace(/,/g, "");

        // Buat FormData baru
        const formData = new FormData();

        // Ambil semua field text/select (bukan file)
        const formElements = form.elements;
        for (let element of formElements) {
            if (element.type !== 'file' && element.name) {
                formData.append(element.name, element.value);
            }
        }

        // Tambahkan HANYA file yang valid (ada isinya)
        const allFileInputs = [
            mainInput,
            document.getElementById("extraImage1"),
            document.getElementById("extraImage2"),
            document.getElementById("extraImage3"),
            document.getElementById("extraImage4")
        ];

        let validFileCount = 0;
        allFileInputs.forEach(input => {
            if (input && input.files.length > 0 && input.files[0].size > 0) {
                formData.append('allProductImage', input.files[0]);
                validFileCount++;
            }
        });

        console.log(`Mengirim ${validFileCount} file gambar yang valid`);

        // Validasi minimal 1 gambar
        if (validFileCount === 0) {
            alert('Minimal harus ada 1 gambar produk!');
            return;
        }

        // Kirim via fetch
        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                // Redirect ke halaman success atau produk list
                window.location.href = response.url || '/admin/products';
            } else {
                const errorText = await response.text();
                alert('Gagal menyimpan produk: ' + errorText);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Terjadi kesalahan saat mengirim data');
        }
    });
});

// 🟢 Fungsi di bawah ini dideklarasikan di global scope
const categoryDialog = document.getElementById('categoryDialog');
const categorySelect = document.getElementById('categorySelect');

function handleCategorySelect(select) {
    if (select.value === 'new') {
        categoryDialog.showModal();
    }
}

function closeCategoryDialog() {
    categoryDialog.close();
    categorySelect.value = '';
}

async function submitCategory(e) {
    e.preventDefault();
    const name = document.getElementById('categoryName').value.trim();
    const description = document.getElementById('categoryDesc').value.trim();

    if (!name) return alert('Nama kategori wajib diisi.');

    const res = await fetch('/admin/category/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, description })
    });

    if (res.ok) {
        closeCategoryDialog();
        await refreshCategoryOptions();
    } else {
        alert('Gagal menambah kategori.');
        console.log(res);
    }
}

async function refreshCategoryOptions() {
    const res = await fetch('/category/get-all');
    if (!res.ok) return alert('Gagal mengambil data kategori.');

    const categories = await res.json();
    const select = document.getElementById('categorySelect');
    select.innerHTML = '<option disabled selected>Pilih kategori</option>';

    categories.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = cat.id;
        opt.textContent = cat.name;
        select.appendChild(opt);
    });

    const addOpt = document.createElement('option');
    addOpt.value = 'new';
    addOpt.textContent = '+ Tambah Kategori Baru';
    select.appendChild(addOpt);
}