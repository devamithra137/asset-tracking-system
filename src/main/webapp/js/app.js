/* ================================
 * Asset Tracking System - app.js
 * FINAL (ES5-compatible + matches your HTML)
 * ================================ */
(function () {
  // ---------- CONFIG ----------
  var CONTEXT = "assettrackingsystem";
  var API = "/" + CONTEXT + "/api";
  console.log("API Base URL =", API);

  // ---------- STATE ----------
  var currentUser = null;
  var currentAssetForEdit = null;

  // ---------- DOM HELPERS ----------
  function byId(id) { return document.getElementById(id); }
  function setText(id, v) { var el = byId(id); if (el) el.textContent = v; }
  function qs(sel, root) { return (root || document).querySelector(sel); }
  function qsa(sel, root) { return (root || document).querySelectorAll(sel); }
  function getAssetFormField(name) { return qs('#assetForm [name="' + name + '"]'); }
  function setAssetField(name, v) { var el = getAssetFormField(name); if (el) el.value = (v == null ? "" : v); }
  function getAssetFieldValue(name) { var el = getAssetFormField(name); return el ? el.value : ""; }

  function safe(v) {
    if (v === null || v === undefined) return "";
    return String(v).replace(/[&<>"']/g, function (c) {
      return { "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#39;" }[c];
    });
  }

  // ---------- STORAGE / NAV ----------
  function getStoredUser() {
    try { var s = localStorage.getItem("user"); if (s) return JSON.parse(s); } catch (e) {}
    return null;
  }
  function clearUser() { try { localStorage.removeItem("user"); } catch (e) {} }
  function go(page) { window.location.href = "/" + CONTEXT + "/" + page; }

  // ---------- FETCH (always include cookies) ----------
  function fetchJson(url, options) {
    var opts = options || {};
    if (!opts.credentials) opts.credentials = "include";

    return fetch(url, opts).then(function (res) {
      var ct = res.headers.get("content-type") || "";
      var isJson = ct.indexOf("application/json") !== -1;

      if (!res.ok) {
        if (isJson) {
          return res.json().catch(function () { return {}; }).then(function (body) {
            var err = new Error("HTTP " + res.status + " on " + url);
            err.status = res.status; err.body = body; throw err;
          });
        }
        var err2 = new Error("HTTP " + res.status + " on " + url);
        err2.status = res.status; err2.body = {}; throw err2;
      }

      if (res.status === 204) return {};
      return isJson ? res.json() : {};
    });
  }

  // ---------- SESSION GUARD ----------
  document.addEventListener("DOMContentLoaded", function () {
    currentUser = getStoredUser();

    var path = (window.location.pathname || "").toLowerCase();
    var isLogin = path.indexOf("login.html") !== -1 || path.slice(-CONTEXT.length - 1) === ("/" + CONTEXT + "/");

    if (!isLogin && !currentUser) { go("login.html"); return; }
    if (isLogin && currentUser) { redirectToDashboard(currentUser.role); return; }

    if (!isLogin && currentUser) initializeDashboard();
  });

  function redirectToDashboard(role) {
    if (role === "ADMINISTRATOR") { go("admin-dashboard.html"); return; }
    if (role === "INVENTORY_MANAGER") { go("manager-dashboard.html"); return; }
    go("user-dashboard.html");
  }

  // ---------- DASHBOARD ----------
  function initializeDashboard() {
    if (byId("userInfo") && currentUser) {
      setText("userInfo", (currentUser.fullName || currentUser.username) + " (" + currentUser.role + ")");
    }

    setupTabNavigation();

    var logoutBtn = byId("logoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", logout);

    // Hook your buttons/filters per your HTML
    var addAssetBtn = byId("addAssetBtn"); // <-- your HTML uses this id
    if (addAssetBtn) addAssetBtn.addEventListener("click", openAddAssetModal);

    // One filter: statusFilter (your HTML)
    var statusFilter = byId("statusFilter");
    if (statusFilter) statusFilter.addEventListener("change", applyAssetFilters);

    // Handle asset form submit (prevents page reload)
    var assetForm = byId("assetForm");
    if (assetForm) {
      assetForm.addEventListener("submit", function (e) {
        e.preventDefault();
        if (currentAssetForEdit) handleEditAsset(); else handleAddAsset();
      });
    }

    loadDashboardData();

    // Load first active tab
    var firstActive = qs(".nav-menu a.active");
    if (firstActive) loadTabData(firstActive.getAttribute("data-tab"));
  }

  function setupTabNavigation() {
    var links = qsa(".nav-menu a");
    Array.prototype.forEach.call(links, function (link) {
      link.addEventListener("click", function (e) {
        e.preventDefault();
        Array.prototype.forEach.call(links, function (l) { l.classList.remove("active"); });
        link.classList.add("active");
        Array.prototype.forEach.call(qsa(".tab-content"), function (t) { t.classList.remove("active"); });
        var tabId = link.getAttribute("data-tab");
        var dst = byId(tabId); if (dst) dst.classList.add("active");
        loadTabData(tabId);
      });
    });
  }

  function loadDashboardData() {
    if (!currentUser) return;
    if (currentUser.role === "ADMINISTRATOR" || currentUser.role === "INVENTORY_MANAGER") loadAdminStats();
    else loadUserStats();
  }

  function loadAdminStats() {
    fetchJson(API + "/assets/").then(function (assets) {
      setText("totalAssets", assets.length);
      setText("availableAssets", assets.filter(function (a) { return a.status === "AVAILABLE"; }).length);
      setText("assignedAssets", assets.filter(function (a) { return a.status === "ASSIGNED"; }).length);
    })["catch"](function () {});

    fetchJson(API + "/requests/pending").then(function (reqs) {
      setText("pendingRequests", reqs.length);
    })["catch"](function () {});
  }

  function loadUserStats() {
    fetchJson(API + "/assets/user/" + currentUser.userId).then(function (assets) {
      setText("myAssetsCount", assets.length);
    })["catch"](function () {});

    fetchJson(API + "/requests/user/" + currentUser.userId).then(function (reqs) {
      var count = reqs.filter(function (r) { return r.status === "PENDING"; }).length;
      setText("pendingRequestsCount", count);
    })["catch"](function () {});

    fetchJson(API + "/assets/status/AVAILABLE").then(function (assets) {
      setText("availableAssetsCount", assets.length);
    })["catch"](function () {});
  }

  // ---------- TABS ----------
  function loadTabData(tabId) {
    if (tabId === "assets") loadAllAssets();
    if (tabId === "requests") loadRequests();
    if (tabId === "users") loadUsers(); // requires /api/users and ADMIN on backend
  }

  // ---------- FILTERS ----------
  function getAssetFilterUrl() {
    // Your HTML only has #statusFilter and #assetSearch
    var search = (byId("assetSearch") && byId("assetSearch").value) || "";
    var status = (byId("statusFilter") && byId("statusFilter").value) || "";

    var qsParts = [];
    if (search) qsParts.push("search=" + encodeURIComponent(search));
    if (status) qsParts.push("status=" + encodeURIComponent(status));

    return API + "/assets/" + (qsParts.length ? ("?" + qsParts.join("&")) : "");
  }
  function applyAssetFilters() { loadAllAssets(); }

  // ---------- LOADERS ----------
  function loadAllAssets() {
    fetchJson(getAssetFilterUrl()).then(function (assets) {
      var tbody = byId("assetsTableBody");
      if (!tbody) return;
      tbody.innerHTML = assets.map(function (a) {
        var status = (a.status || "").toLowerCase();
        return '' +
          '<tr>' +
            '<td>' + safe(a.assetTag) + '</td>' +
            '<td>' + safe(a.assetName) + '</td>' +
            '<td>' + safe(a.category) + '</td>' +
            '<td><span class="status-badge status-' + safe(status) + '">' + safe(a.status) + '</span></td>' +
            '<td>' + safe(a.location) + '</td>' +
            '<td>' + (a.assignedTo || "-") + '</td>' +
            '<td>' +
              '<button class="btn btn-sm btn-primary" data-edit="' + a.assetId + '">Edit</button> ' +
              '<button class="btn btn-sm btn-danger" data-del="' + a.assetId + '">Delete</button>' +
            '</td>' +
          '</tr>';
      }).join("");

      // delegate actions
      tbody.querySelectorAll('[data-edit]').forEach(function (b) {
        b.addEventListener('click', function () { openEditAssetModal(b.getAttribute('data-edit')); });
      });
      tbody.querySelectorAll('[data-del]').forEach(function (b) {
        b.addEventListener('click', function () { deleteAsset(b.getAttribute('data-del')); });
      });
    })["catch"](function (e) { console.error("loadAllAssets", e); });
  }

  function loadRequests() {
    var url = (currentUser.role === "USER")
      ? (API + "/requests/user/" + currentUser.userId)
      : (API + "/requests/");
    fetchJson(url).then(function (list) {
      var body = byId("requestsTableBody"); if (!body) return;
      body.innerHTML = list.map(function (r) {
        var canAct = (r.status === "PENDING" &&
                      (currentUser.role === "ADMINISTRATOR" || currentUser.role === "INVENTORY_MANAGER"));
        return '' +
          '<tr>' +
            '<td>' + r.requestId + '</td>' +
            '<td>' + safe(r.userName || 'N/A') + '</td>' +
            '<td>' + safe(r.assetName || 'N/A') + '</td>' +
            '<td>' + safe(r.requestType) + '</td>' +
            '<td>' + safe(r.date || '-') + '</td>' +
            '<td><span class="status-badge status-' + safe((r.status||'').toLowerCase()) + '">' + safe(r.status) + '</span></td>' +
            '<td>' + (canAct
              ? '<button class="btn btn-sm btn-success" data-approve="'+r.requestId+'" data-asset="'+r.assetId+'" data-user="'+r.userId+'" data-type="'+safe(r.requestType)+'">Approve</button> ' +
                '<button class="btn btn-sm btn-danger" data-reject="'+r.requestId+'" data-asset="'+r.assetId+'" data-user="'+r.userId+'" data-type="'+safe(r.requestType)+'">Reject</button>'
              : '-') + '</td>' +
          '</tr>';
      }).join("");

      body.querySelectorAll('[data-approve]').forEach(function (b) {
        b.addEventListener('click', function () {
          approveRequest(b.getAttribute('data-approve'), b.getAttribute('data-asset'), b.getAttribute('data-user'), b.getAttribute('data-type'));
        });
      });
      body.querySelectorAll('[data-reject]').forEach(function (b) {
        b.addEventListener('click', function () {
          rejectRequest(b.getAttribute('data-reject'), b.getAttribute('data-asset'), b.getAttribute('data-user'), b.getAttribute('data-type'));
        });
      });
    })["catch"](function (e) { console.error("loadRequests", e); });
  }

  function loadUsers() {
    // Requires backend: GET /api/users (403 if not ADMIN)
    fetchJson(API + "/users").then(function (users) {
      var body = byId("usersTableBody"); if (!body) return;
      body.innerHTML = users.map(function (u) {
        return '' +
          '<tr>' +
            '<td>' + u.userId + '</td>' +
            '<td>' + safe(u.username) + '</td>' +
            '<td>' + safe(u.fullName) + '</td>' +
            '<td>' + safe(u.email) + '</td>' +
            '<td>' + safe(u.role) + '</td>' +
            '<td><span class="status-badge status-' + (u.isActive ? 'approved' : 'rejected') + '">' + (u.isActive ? 'Active' : 'Inactive') + '</span></td>' +
            '<td>' +
              '<button class="btn btn-sm btn-primary" data-u-edit="'+u.userId+'">Edit</button> ' +
              '<button class="btn btn-sm btn-danger" data-u-toggle="'+u.userId+'" data-active="'+(u.isActive?1:0)+'">'+(u.isActive?'Deactivate':'Activate')+'</button>' +
            '</td>' +
          '</tr>';
      }).join("");

      body.querySelectorAll('[data-u-edit]').forEach(function (b) {
        b.addEventListener('click', function () {
          openEditUserModal(b.getAttribute('data-u-edit'));
        });
      });
      body.querySelectorAll('[data-u-toggle]').forEach(function (b) {
        b.addEventListener('click', function () {
          toggleUserStatus(b.getAttribute('data-u-toggle'), b.getAttribute('data-active'));
        });
      });
    })["catch"](function (e) {
      console.error("loadUsers", e);
    });
  }

  // ---------- MODALS ----------
  function openAddAssetModal() {
    currentAssetForEdit = null;
    var f = byId("assetForm"); if (f) f.reset();
    setText("assetModalTitle", "Add New Asset");             // your HTML id
    openModal("assetModal");
  }

  function openEditAssetModal(assetId) {
    fetchJson(API + "/assets/" + assetId).then(function (asset) {
      currentAssetForEdit = asset;
      setText("assetModalTitle", "Edit Asset");              // your HTML id

      setAssetField("assetName", asset.assetName);
      setAssetField("assetTag", asset.assetTag);
      setAssetField("category", asset.category);
      setAssetField("description", asset.description);
      setAssetField("purchaseDate", asset.purchaseDate ? String(asset.purchaseDate).split("T")[0] : "");
      setAssetField("purchaseCost", asset.purchaseCost);
      setAssetField("currentValue", asset.currentValue);
      setAssetField("status", asset.status);
      setAssetField("location", asset.location);

      openModal("assetModal");
    })["catch"](function () { alert("Failed to load asset."); });
  }

  // ---------- ASSET ACTIONS ----------
  function handleAddAsset() {
    var form = byId("assetForm"); if (!form) return;
    var body = new URLSearchParams(new FormData(form)).toString();

    fetchJson(API + "/assets/", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: body
    }).then(function (res) {
      alert((res && res.message) || "Asset created.");
      closeModal("assetModal");
      loadAllAssets();
    })["catch"](function (err) {
      alert("Error: " + ((err.body && err.body.message) || "Failed to add asset."));
    });
  }

  function handleEditAsset() {
    if (!currentAssetForEdit) { alert("Asset not loaded"); return; }
    var form = byId("assetForm"); if (!form) return;

    var fd = new FormData(form), payload = {};
    fd.forEach(function (v, k) { payload[k] = v; });

    fetchJson(API + "/assets/" + currentAssetForEdit.assetId, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }).then(function (res) {
      alert((res && res.message) || "Asset updated.");
      closeModal("assetModal");
      loadAllAssets();
    })["catch"](function (err) {
      alert("Error: " + ((err.body && err.body.message) || "Failed to update asset."));
    });
  }

  function deleteAsset(assetId) {
    if (!confirm("Delete this asset permanently?")) return;
    fetchJson(API + "/assets/" + assetId, { method: "DELETE" })
      .then(function (res) {
        alert((res && res.message) || "Asset deleted.");
        loadAllAssets();
      })["catch"](function (err) {
        alert("Error: " + ((err.body && err.body.message) || "Failed to delete asset."));
      });
  }

  // ---------- REQUEST ACTIONS ----------
  function approveRequest(requestId, assetId, userId, requestType) {
    if (!confirm("Approve Request ID " + requestId + "?")) return;
    updateRequestStatus(requestId, "APPROVED", "", assetId, userId, requestType);
  }
  function rejectRequest(requestId, assetId, userId, requestType) {
    var notes = window.prompt("Reason for REJECT?");
    if (notes === null) return;
    updateRequestStatus(requestId, "REJECTED", notes, assetId, userId, requestType);
  }
  function updateRequestStatus(requestId, status, notes, assetId, userId, requestType) {
    var payload = { status: status, notes: notes || "", assetId: assetId, userId: userId, requestType: requestType };
    fetchJson(API + "/requests/" + requestId, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }).then(function (res) {
      alert((res && res.message) || "Request updated.");
      loadRequests(); loadDashboardData();
    })["catch"](function (err) {
      alert("Error updating request: " + ((err.body && err.body.message) || "Server error."));
    });
  }

  // ---------- USERS (optional, if your UI adds a user modal later) ----------
  function openEditUserModal(userId) {
    // add your user modal in HTML if you want to use this
    alert("User modal not present in HTML yet.");
  }
  function toggleUserStatus() {
    alert("User modal not present in HTML yet.");
  }

  // ---------- LOGOUT ----------
  function logout() {
    fetchJson(API + "/auth/logout", { method: "POST" })["catch"](function () {})
      ["finally"](function () { clearUser(); go("login.html"); });
  }

  // expose functions used by inline handlers (if any)
  window.deleteAsset = deleteAsset;
  window.approveRequest = approveRequest;
  window.rejectRequest = rejectRequest;
  window.openEditAssetModal = openEditAssetModal;
})();
