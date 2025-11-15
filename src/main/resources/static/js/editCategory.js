const categoryDialog = document.getElementById('categoryDialog');
const deleteDialog = document.getElementById('deleteCategoryDialog');
const categorySelect = document.getElementById('categorySelect');
let selectedCategoryId = null;


document.addEventListener('DOMContentLoaded', () => {
    const select = document.getElementById('categorySelect');
    if (select.value && select.value !== 'new') {
        document.getElementById('editCategoryBtn').disabled = false;
        document.getElementById('deleteCategoryBtn').disabled = false;
        selectedCategoryId = select.value; // update selectedCategoryId
    }
});

function handleCategorySelect(select) {
    selectedCategoryId = select.value;
    const editBtn = document.getElementById('editCategoryBtn');
    const delBtn = document.getElementById('deleteCategoryBtn');

    if (selectedCategoryId && selectedCategoryId !== 'new') {
        editBtn.disabled = false;
        delBtn.disabled = false;
    } else {
        editBtn.disabled = true;
        delBtn.disabled = true;
    }

    if (selectedCategoryId === 'new') {
        openAddCategoryDialog();
    }
}


// 🔹 Modal Add
function openAddCategoryDialog() {
    document.getElementById('categoryDialogTitle').textContent = 'Tambah Kategori';
    document.getElementById('categoryForm').dataset.mode = 'add';
    document.getElementById('categoryName').value = '';
    document.getElementById('categoryDesc').value = '';
    document.getElementById('categoryId').value = '';
    categoryDialog.showModal();
}

// 🔹 Modal Edit
async function openEditCategoryDialog() {
    if (!selectedCategoryId) return alert('Pilih kategori terlebih dahulu.');

    const res = await fetch(`/category/${selectedCategoryId}`);
    if (!res.ok) return alert('Gagal mengambil data kategori.');
    const cat = await res.json();

    document.getElementById('categoryDialogTitle').textContent = 'Edit Kategori';
    document.getElementById('categoryForm').dataset.mode = 'edit';
    document.getElementById('categoryName').value = cat.name;
    document.getElementById('categoryDesc').value = cat.description || '';
    document.getElementById('categoryId').value = cat.id;

    categoryDialog.showModal();
}

function openDeleteCategoryDialog() {
    if (!selectedCategoryId) return alert('Pilih kategori terlebih dahulu.');
    const selectedText = categorySelect.options[categorySelect.selectedIndex].text;
    document.getElementById('deleteCategoryName').textContent =
        `Apakah kamu yakin ingin menghapus kategori "${selectedText}"?`;
    deleteDialog.showModal();
}

function closeCategoryDialog() {
    categoryDialog.close();
    categorySelect.value = '';
}
function closeDeleteCategoryDialog() {
    deleteDialog.close();
}

// 🔹 Submit Add/Edit
async function submitCategory(e) {
    e.preventDefault();
    const mode = e.target.dataset.mode;
    const id = document.getElementById('categoryId').value;
    const name = document.getElementById('categoryName').value.trim();
    const description = document.getElementById('categoryDesc').value.trim();

    if (!name) return alert('Nama kategori wajib diisi.');

    const url = mode === 'edit' ? `/admin/category/edit/${id}` : '/admin/category/add';
    const method = 'POST';
    const body = mode === 'edit'
        ? new FormData(e.target)
        : JSON.stringify({ name, description });

    const headers = mode === 'edit' ? {} : { 'Content-Type': 'application/json' };

    const res = await fetch(url, { method, headers, body });
    if (res.ok) {
        closeCategoryDialog();
        await refreshCategoryOptions();
    } else {
        alert(mode === 'edit' ? 'Gagal mengedit kategori.' : 'Gagal menambah kategori.');
    }
}

async function confirmDeleteCategory() {
    const res = await fetch(`/admin/category/delete/${selectedCategoryId}`, { method: 'DELETE' });
    if (res.ok) {
        closeDeleteCategoryDialog();
        await refreshCategoryOptions();
    } else {
        alert('Gagal menghapus kategori.');
    }
}

// 🔹 Refresh select
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

    // Reset tombol
    document.getElementById('editCategoryBtn').disabled = true;
    document.getElementById('deleteCategoryBtn').disabled = true;
}

