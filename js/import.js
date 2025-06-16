async function importCSV(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = async function (e) {
    const content = e.target.result;
    const lines = content.split("\n").map(line => line.trim()).filter(Boolean);

    const [headerLine, ...dataLines] = lines;
    const headers = headerLine.split(",").map(h => h.trim());
    const mapping = guessColumnMapping(headers);

    if (
      mapping.title === undefined ||
      mapping.amount === undefined ||
      mapping.category === undefined ||
      mapping.date === undefined
    ) {
      alert("找不到必要欄位（標題、金額、分類、日期），請確認 CSV 格式！");
      return;
    }

    const expenses = dataLines.map(line => {
      const cols = line.split(",").map(c => c.trim());
      return {
        title: cols[mapping.title],
        amount: parseFloat(cols[mapping.amount]),
        category: cols[mapping.category],
        date: new Date(cols[mapping.date]).toISOString().split("T")[0]
      };
    });

    // 取得現有資料
    const existingExpenses = await fetch("/api/expenses").then(res => res.json());
    const existingSet = new Set(
      existingExpenses.map(exp => [exp.title, exp.amount, exp.category, exp.date].join("|"))
    );

    // 過濾重複資料
    const filteredExpenses = expenses.filter(exp => {
      const key = [exp.title, exp.amount, exp.category, exp.date].join("|");
      return !existingSet.has(key);
    });

    let successCount = 0;
    for (const exp of filteredExpenses) {
      if (!exp.title || isNaN(exp.amount) || !exp.category || !exp.date) continue;

      try {
        const res = await fetch("/api/expenses", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(exp)
        });

        if (res.ok) successCount++;
      } catch (err) {
        console.error("匯入失敗：", exp, err);
      }
    }

    const skippedCount = expenses.length - filteredExpenses.length;
    alert(`成功匯入 ${successCount} 筆資料，跳過 ${skippedCount} 筆重複資料`);
    queryExpenses();
  };

  reader.readAsText(file);
}
