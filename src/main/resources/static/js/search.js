(function () {
    var form = document.getElementById("search-form");
    var fromInput = document.getElementById("from-date");
    var toInput = document.getElementById("to-date");
    var button = document.getElementById("search-button");
    var formError = document.getElementById("form-error");
    var resultPanel = document.getElementById("result-panel");
    var summary = document.getElementById("result-summary");
    var missingNote = document.getElementById("missing-note");
    var emptyNote = document.getElementById("empty-note");
    var table = document.getElementById("result-table");
    var tbody = document.getElementById("result-body");
    var totalCount = document.getElementById("total-count");
    var buttonLabel = button.textContent;
    var maxMonths = Number(form.getAttribute("data-max-months")) || 12;

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        var from = fromInput.value;
        var to = toInput.value;
        var validation = validate(from, to);
        if (validation) {
            showError(validation);
            return;
        }
        hide(formError);
        search(from, to);
    });

    function validate(from, to) {
        if (!from || !to) {
            return "시작일과 종료일을 선택해 주세요.";
        }
        if (from > to) {
            return "종료일은 시작일보다 빠를 수 없습니다.";
        }
        if (monthSpan(from, to) > maxMonths) {
            return "조회 기간은 최대 " + maxMonths + "개월까지 가능합니다.";
        }
        return "";
    }

    function monthSpan(from, to) {
        var startYear = Number(from.slice(0, 4));
        var startMonth = Number(from.slice(5, 7));
        var endYear = Number(to.slice(0, 4));
        var endMonth = Number(to.slice(5, 7));
        return (endYear - startYear) * 12 + (endMonth - startMonth) + 1;
    }

    function search(from, to) {
        button.disabled = true;
        button.textContent = "조회 중";
        var params = new URLSearchParams();
        params.set("from", from);
        params.set("to", to);
        fetch("/api/result-codes?" + params.toString(), {
            headers: { "Accept": "application/json" }
        }).then(function (response) {
            return response.json().then(function (body) {
                return { ok: response.ok, body: body };
            });
        }).then(function (result) {
            if (!result.ok) {
                showError(result.body && result.body.message
                    ? result.body.message
                    : "조회에 실패했습니다.");
                return;
            }
            render(result.body);
        }).catch(function () {
            showError("서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }).finally(function () {
            button.disabled = false;
            button.textContent = buttonLabel;
        });
    }

    function render(data) {
        hide(formError);
        summary.replaceChildren();
        addSummary("조회 기간", formatDate(data.from) + " ~ " + formatDate(data.to));
        addSummary("총 건수", formatNumber(data.totalCount) + "건");
        addSummary("사용 테이블", (data.tables || []).join(", "));

        if (data.missingTables && data.missingTables.length > 0) {
            missingNote.textContent = "테이블이 없어 제외한 달: " + data.missingTables.join(", ");
            show(missingNote);
        } else {
            hide(missingNote);
        }

        tbody.replaceChildren();
        var rows = data.rows || [];
        if (rows.length === 0) {
            show(emptyNote);
            hide(table);
        } else {
            hide(emptyNote);
            show(table);
            rows.forEach(function (row) {
                var tr = document.createElement("tr");
                tr.appendChild(textCell(displayCode(row.resultCode), false));
                tr.appendChild(textCell(formatNumber(row.count), true));
                tr.appendChild(textCell(formatRatio(row.count, data.totalCount), true));
                tbody.appendChild(tr);
            });
        }
        totalCount.textContent = formatNumber(data.totalCount);
        show(resultPanel);
    }

    function addSummary(label, value) {
        var term = document.createElement("dt");
        term.textContent = label;
        var description = document.createElement("dd");
        description.textContent = value;
        summary.appendChild(term);
        summary.appendChild(description);
    }

    function textCell(value, numeric) {
        var cell = document.createElement("td");
        cell.textContent = value;
        if (numeric) {
            cell.className = "num";
        }
        return cell;
    }

    function displayCode(resultCode) {
        if (resultCode == null || String(resultCode).trim() === "") {
            return "(값 없음)";
        }
        return String(resultCode);
    }

    function formatDate(iso) {
        if (!iso || iso.length < 10) {
            return "";
        }
        return Number(iso.slice(0, 4)) + "년 "
            + Number(iso.slice(5, 7)) + "월 "
            + Number(iso.slice(8, 10)) + "일";
    }

    function formatNumber(value) {
        return Number(value || 0).toLocaleString("ko-KR");
    }

    function formatRatio(count, total) {
        if (!total) {
            return "-";
        }
        return (count * 100 / total).toLocaleString("ko-KR", {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1
        }) + "%";
    }

    function showError(message) {
        formError.textContent = message;
        show(formError);
        hide(resultPanel);
    }

    function show(element) {
        element.hidden = false;
    }

    function hide(element) {
        element.hidden = true;
    }
})();
