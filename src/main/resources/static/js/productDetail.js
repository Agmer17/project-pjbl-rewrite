const adminBtn = document.getElementById("btnOpenAdminModal");
const adminModal = document.getElementById("adminModal");
const closeBtn = document.getElementById("closeAdminModal");
const adminList = document.getElementById("adminList");

const productId = document.getElementById("productId")?.value;

adminBtn?.addEventListener("click", async () => {
  adminModal.showModal();
  loadAdmins();
});

closeBtn?.addEventListener("click", () => adminModal.close());


async function loadAdmins() {
  adminList.innerHTML = `<span class='loading loading-spinner loading-md'></span>`;

  try {
    const res = await fetch("/user/get-all-admins");
    const admins = await res.json();

    if (!admins || admins.length === 0) {
      adminList.innerHTML = `<p class='text-sm text-neutral/60'>Tidak ada admin tersedia.</p>`;
      return;
    }

    adminList.innerHTML = "";

    admins.forEach(admin => {
      adminList.innerHTML += `
                      <button 
  class="w-full justify-start bg-base-200 text-base-100 hover:bg-neutral/80 hover:cursor-pointer
         rounded-xl flex gap-3 p-4"  onclick="selectAdmin('${admin.id}')">

                        <div class="avatar">
                          <div class="w-10 rounded-full">
                            <img src="${admin.profilePicture ? `/uploads/${admin.profilePicture}` : `/img/default.jpg`}" />

                          </div>
                        </div>

                        <div class="flex flex-col text-left">
                          <span class="font-semibold text-neutral">${admin.fullName}</span>
                          <span class="text-xs text-neutral">${admin.email}</span>
                        </div>
                      </button>
                  `;
    });
  } catch (e) {
    console.error(e);
    adminList.innerHTML = `<p class='text-sm text-neutral/60'>Gagal memuat admin.</p>`;
  }
}


function selectAdmin(userId) {
  if (!productId) {
    alert("Product ID tidak tersedia");
    return;
  }

  // Redirect ke endpoint livechat
  window.location.href = `/livechat/redirect/p/${productId}/u/${userId}`;
}