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

    priceInput.addEventListener("input", (e) => {
        let value = e.target.value.replace(/\./g, "");
        if (!isNaN(value) && value.length > 0) {
            e.target.value = Number(value).toLocaleString("id-ID");
        } else {
            e.target.value = "";
        }
    });

    form?.addEventListener("submit", (e) => {
        e.preventDefault();

        priceInput.value = priceInput.value.replace(/\./g, "")

        // Validasi gambar
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
                validFileCount++;
            }
        });

        console.log(`Mengirim ${validFileCount} file gambar yang valid`);

        // Validasi minimal 1 gambar
        if (validFileCount === 0) {
            alert('Minimal harus ada 1 gambar produk!');
            return;
        }

        // Hapus input file yang kosong agar tidak dikirim
        allFileInputs.forEach(input => {
            if (input && (!input.files.length || input.files[0].size === 0)) {
                input.removeAttribute('name'); // Hapus name agar tidak dikirim
                // ATAU input.disabled = true;
            }
        });

        form.submit();
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