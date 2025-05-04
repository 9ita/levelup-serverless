function jobRowContextMenu() {
  return [
    {
      label: `<i class="material-symbols-rounded">approval_delegation</i> Job Approval (승인 요청)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();

        document.getElementById('modalApprove_jobid').value = rowData.id;

        bootstrap.Modal.getInstance(document.getElementById('approveModal')).show();
      }
    },
    {separator: true},
    {
      label: `<i class="material-symbols-rounded">play_arrow</i> Job Run (시작)`,
      action: async function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        selectedJobTag = rowData.jobTag || "";
        selectedJobName = rowData.jobName || "";

        document.getElementById('jobNameInput').value = selectedJobName;
        document.getElementById('jobReasonInput').value = "";
        document.getElementById('validDateInput').value = "";
        document.getElementById('validHourInput').value = "";
        document.getElementById('validMinInput').value = "";

        bootstrap.Modal.getInstance(document.getElementById('jobRunModal')).show();
      }
    },
    {
      label: `<i class="material-symbols-rounded">file_copy</i> Job Copy (복사)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();

        document.getElementById('originalJobId').value = rowData.id;
        document.getElementById('copyJobName').value = rowData.jobName;

        bootstrap.Modal.getInstance(document.getElementById('copyModal')).show();
      }
    },
    {
      label: `<i class="material-symbols-rounded">info</i> Properties (속성)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        window.location.href = "/job/" + rowData.id;
      }
    },
    {separator: true},
    // 그룹 3: Job Search, Job Query Search, Refresh, Job Log
    {
      label: `<i class="material-symbols-rounded">search</i> Job Search (검색)`,
      action: function (e, row) {
        e.preventDefault();
        bootstrap.Modal.getInstance(document.getElementById('searchModal')).show();
      }
    },
    {
      label: `<i class="material-symbols-rounded">search</i> Job Query Search (검색)`,
      action: function (e, row) {
        e.preventDefault();
        alert("TODO");
      }
    },
    {
      label: `<i class="material-symbols-rounded">refresh</i> Refresh (새로고침)`,
      action: function (e, row) {
        e.preventDefault();
        searchStandard('#searchCondition', window.grid, '/api/v1/jobs/grid/tree');
      }
    },
    {
      label: `<i class="material-symbols-rounded">receipt_long</i> Job Log (로그)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        window.location.href = `/resultsLog?jobName=${rowData.jobName}`;
      }
    },
    {separator: true},
    // 그룹 4: Job Kill, Job Delete, Schedule
    {
      label: `<i class="material-symbols-rounded">cancel</i> Job Kill (중지)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        fetchStandard(`/api/v1/jobs/${rowData.id}/kill`, "POST");
      }
    },
    {
      label: `<i class="material-symbols-rounded">delete</i> Job Delete (삭제)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();

        if (!confirmStandard()) {
          return;
        }
        fetchStandard(`/api/v1/jobs/${rowData.id}`, "DELETE").then(r => window.location.reload());
      }
    },
    {
      label: `<i class="material-symbols-rounded">edit_calendar</i> Schedule (스케줄등록)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        bootstrap.Modal.getInstance(document.getElementById('scheduleModal')).show();
      }
    },
    {separator: true},
    // 그룹 5: Job Add
    {
      label: `<i class="material-symbols-rounded">playlist_add</i> Job Setting (등록)`,
      action: function (e, row) {
        e.preventDefault();
        const rowData = row.getData();
        window.location.href = "/job/new";
      }
    }
  ];
}

let selectedJobTag = "";
let selectedJobName = "";

function submitJobRun(rerunFlag) {
  var jobReason = document.getElementById('jobReasonInput').value;
  var validDate = document.getElementById('validDateInput').value; // yyyy-MM-dd
  var validHour = document.getElementById('validHourInput').value;
  var validMin = document.getElementById('validMinInput').value;

  let validPeriod = "";
  if (validDate) {
    validPeriod = validDate.replace(/-/g, "");
    validPeriod += (validHour ? validHour.padStart(2, "0") : "00");
    validPeriod += (validMin ? validMin.padStart(2, "0") : "00");
  }

  var now = new Date();
  var ymd = now.toISOString().slice(0, 10).replace(/-/g, "");
  var time = now.toTimeString().slice(0, 8).replace(/:/g, "");

  const jobRunRequest = {
    auto: "N",
    ymd: ymd,
    time: time,
    srno: validPeriod,   // 유효기간
    reason: jobReason,
    rerun: rerunFlag,
    nodeTagStr: selectedJobTag
  };

  fetchStandard("/api/v1/jobs/run", "POST", JSON.stringify(jobRunRequest), window.location.reload);

}

function submitJobApproval() {
  // 모달 입력 값 가져오기
  const approveName = document.getElementById('modalApprove_name').value;
  const approveReason = document.getElementById('modalApprove_reason').value;
  const approveRequest = document.getElementById('modalApprove_request').value;
  const moveRealData = document.getElementById('modalApprove_moveRealData').checked ? "Y" : "N";
  const dataValidDate = document.getElementById('modalApprove_dataValidDate').value;

  // 날짜 형식 처리 yyyyMMdd
  const validPeriod = dataValidDate ? dataValidDate.replace(/-/g, "") : "";

  // 현재 시간 처리
  const now = new Date();
  const ymd = now.toISOString().slice(0, 10).replace(/-/g, "");
  const time = now.toTimeString().slice(0, 8).replace(/:/g, "");

  // 요청 객체 생성
  const jobApprovalRequest = {
    jobid: document.getElementById('modalApprove_jobid').value,
    title: approveName,
    reason: approveReason,
    requestReason: approveRequest,
    moveRealData: moveRealData,
    dataValidDate: validPeriod,
    ymd: ymd,
    time: time,
    nodeTagStr: selectedJobTag
  };

  // API 호출
  fetchStandard("/api/v1/approvals", "POST", JSON.stringify(jobApprovalRequest), () => {
    alert("결재 요청이 완료되었습니다.");
    location.reload();
  });
}

function submitJobCopy() {
  let body = getFormDataToJson('copyForm');
  let originalJobId = document.getElementById('originalJobId').value;
  fetchStandard(`/api/v1/jobs/${originalJobId}/copy`, 'POST', JSON.stringify(body), () => window.location.reload());
}

// 이벤트 바인딩
window.addEventListener('DOMContentLoaded', () => {

  document.getElementById('runBtn').addEventListener('click', (e) => submitJobRun("N"));
  document.getElementById('rerunBtn').addEventListener('click', (e) => submitJobRun("Y"));
  document.getElementById('modalApprove_submit').addEventListener('click', submitJobApproval);
  document.getElementById('modalCopy_submit').addEventListener('click', submitJobCopy);
  document.getElementById('modalSearch');

  // Modal Init
  new bootstrap.Modal(document.getElementById('jobRunModal'));
  new bootstrap.Modal(document.getElementById('approveModal'));
  new bootstrap.Modal(document.getElementById('copyModal'));
  new bootstrap.Modal(document.getElementById('searchModal'));
  new bootstrap.Modal(document.getElementById('scheduleModal'));

});
