// 전역 시간 변수들 선언
let globalStartTime = startTime;
let globalLunchTime = lunchTime;
let globalEndTime = endTime;

// 캘린더 관련 함수들
function updateCalendar(feelingList = []) {
    const today = new Date();
    const monday = new Date(today);
    const diff = today.getDay() === 0 ? 6 : today.getDay() - 1;
    monday.setDate(today.getDate() - diff);

    const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월",
        "7월", "8월", "9월", "10월", "11월", "12월"];
    document.querySelector('.today-date').textContent =
        `${monday.getFullYear()}년 ${monthNames[monday.getMonth()]}`;

    const dateGrid = document.querySelector('.calendar-grid:last-child');
    dateGrid.innerHTML = '';

    for (let i = 0; i < 7; i++) {
        const currentDate = new Date(monday);
        currentDate.setDate(monday.getDate() + i);

        const dateSpan = document.createElement('span');
        dateSpan.className = 'calendar-date';
        dateSpan.textContent = currentDate.getDate();

        if (currentDate.toDateString() === today.toDateString()) {
            dateSpan.classList.add('active');
        }

        const formattedDate = currentDate.toISOString().split('T')[0];
        const feelingData = feelingList.find(item => {
            const itemDate = new Date(item.date).toISOString().split('T')[0];
            return itemDate === formattedDate;
        });

        if (feelingData) {
            dateSpan.classList.remove('verybad', 'bad', 'good', 'verygood');
            const feelingNum = feelingData.feeling_num;
            if (feelingNum <= -6) dateSpan.classList.add('verybad');
            else if (feelingNum >= -5 && feelingNum <= -2) dateSpan.classList.add('bad');
            else if (feelingNum >= 2 && feelingNum <= 5) dateSpan.classList.add('good');
            else if (feelingNum >= 6) dateSpan.classList.add('verygood');
        }

        dateGrid.appendChild(dateSpan);
    }
}

function initializeCalendar(feelingList = []) {
    updateCalendar(feelingList);

    function scheduleNextUpdate() {
        const now = new Date();
        const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
        const timeUntilMidnight = tomorrow - now;

        setTimeout(() => {
            updateCalendar(feelingList);
            scheduleNextUpdate();
        }, timeUntilMidnight);
    }

    scheduleNextUpdate();
}

// 스트레스 그래프 관련 함수들
function updateCircleProgress(circle, progress) {
    // 진행률을 0-100 사이로 제한
    progress = Math.min(100, Math.max(0, progress));

    const circumference = parseFloat(circle.getAttribute('stroke-dasharray'));
    const offset = circumference - (progress / 100) * circumference;
    circle.setAttribute('stroke-dashoffset', offset);

    // 점 위치 업데이트
    const dot = circle.nextElementSibling;
    if (dot && dot.classList.contains('circle-dot')) {
        const angle = (progress / 100) * 360 - 90; // -90도 오프셋으로 시작 위치 조정
        const radius = parseFloat(circle.getAttribute('r'));
        const cx = 100; // SVG 중심 X
        const cy = 100; // SVG 중심 Y

        // 극좌표를 데카르트 좌표로 변환
        const x = cx + radius * Math.cos(angle * Math.PI / 180);
        const y = cy + radius * Math.sin(angle * Math.PI / 180);

        dot.setAttribute('cx', x);
        dot.setAttribute('cy', y);
    }
}

function calculateTimeProgress(startTime, endTime) {
    const now = new Date();
    const [startHour, startMinute] = startTime.split(':').map(Number);
    const [endHour, endMinute] = endTime.split(':').map(Number);

    const start = startHour * 60 + startMinute;
    const end = endHour * 60 + endMinute;
    const current = now.getHours() * 60 + now.getMinutes();

    if (current < start) return 0;
    if (current > end) return 100;

    return ((current - start) / (end - start)) * 100;
}

function calculateWeeklyAverageStress(monthlyStressList) {
    const today = new Date();
    const monday = new Date(today);
    const diff = today.getDay() === 0 ? 6 : today.getDay() - 1;
    monday.setDate(today.getDate() - diff);

    const mondayStr = monday.toISOString().split('T')[0];
    const todayStr = today.toISOString().split('T')[0];

    const weeklyStress = monthlyStressList.filter(item => {
        const itemDate = item.date;
        return itemDate >= mondayStr && itemDate <= todayStr;
    });

    if (weeklyStress.length === 0) return null;

    const sum = weeklyStress.reduce((acc, item) => acc + Number(item.stress_num), 0);
    const average = sum / weeklyStress.length;

    return Math.round(average);
}

function formatTimeRemaining(start, end) {
    const [startHour, startMinute] = start.split(':').map(Number);
    const [endHour, endMinute] = end.split(':').map(Number);
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    const endTimeMinutes = endHour * 60 + endMinute;
    const currentTimeMinutes = currentHour * 60 + currentMinute;

    let remainingMinutes = endTimeMinutes - currentTimeMinutes;
    if (remainingMinutes < 0) remainingMinutes = 0;

    const hours = Math.floor(remainingMinutes / 60);
    const minutes = remainingMinutes % 60;

    return `${hours}시간 ${minutes}분`;
}

function updateAllProgress() {
    const circles = document.querySelectorAll('.progress-circle');
    const timeTexts = document.querySelectorAll('.time-text');
    const stressNoDataElement = document.querySelector('.stress-section .stress-no-data');
    const stressLabelItem = document.querySelector('.label-item:nth-child(2)');

    // 퇴근까지 남은 시간 (큰 원)
    const workProgress = calculateTimeProgress(globalStartTime, globalEndTime);
    updateCircleProgress(circles[0], workProgress);

    // 점심까지 남은 시간 (작은 원)
    const lunchProgress = calculateTimeProgress(globalStartTime, globalLunchTime);
    updateCircleProgress(circles[2], lunchProgress);

    // 시간 텍스트 업데이트 (퇴근, 점심)
    if (timeTexts.length >= 3) {
        timeTexts[0].textContent = formatTimeRemaining(globalStartTime, globalEndTime);
        timeTexts[2].textContent = formatTimeRemaining(globalStartTime, globalLunchTime);
    }

    // 퇴사지수 부분만 조건부 처리
    if (stressNum === null || stressNum === undefined) {
        // 퇴사지수 라벨 텍스트 변경
        if (stressLabelItem) {
            const timeTextElement = stressLabelItem.querySelector('.time-text');
            if (timeTextElement) {
                timeTextElement.textContent = "아직 기록된 퇴사지수가 없어요";
                timeTextElement.style.fontSize = '12px';
            }
        }

        // 퇴사지수 원은 0으로 설정
        if (circles[1]) {
            updateCircleProgress(circles[1], 0);
        }
    } else {
        // 퇴사지수가 있는 경우 정상 표시
        const normalizedStress = ((Number(stressNum) + 10) / 20) * 100;
        updateCircleProgress(circles[1], normalizedStress);

        // 퇴사지수 텍스트 복원
        if (stressLabelItem) {
            const timeTextElement = stressLabelItem.querySelector('.time-text');
            if (timeTextElement) {
                timeTextElement.textContent = `${stressNum > 0 ? '+' : ''}${stressNum}%`;
                timeTextElement.style.fontSize = '';
            }
        }
    }
}

// 초기화 및 이벤트 리스너
document.addEventListener('DOMContentLoaded', () => {
    // 캘린더 초기화
    initializeCalendar(feelingList);

    // 월급일 D-day 계산 및 표시
    const paydayElement = document.getElementById('payday');
    if (paydayElement) {
        const today = new Date();
        const currentYear = today.getFullYear();
        const currentMonth = today.getMonth();
        let payDate = new Date(currentYear, currentMonth, payday);

        if (payDate <= today) {
            payDate = new Date(currentYear, currentMonth + 1, payday);
        }

        const timeDiff = payDate.getTime() - today.getTime();
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));
        paydayElement.innerHTML = `D-${daysDiff}`;
    }

    // 주간 평균 스트레스 계산 및 표시
    const weeklyAverageStress = calculateWeeklyAverageStress(monthlyStressList);
    const stressElement = document.getElementById('weeklyStressNum');
    const overviewStressNoData = document.querySelector('.overview-card .stress-no-data');

    if (stressElement && overviewStressNoData) {
        if (weeklyAverageStress === null) {
            overviewStressNoData.style.display = 'block';
            stressElement.style.display = 'none';
        } else {
            overviewStressNoData.style.display = 'none';
            stressElement.style.display = 'block';
            const sign = weeklyAverageStress > 0 ? '+' : '';
            stressElement.innerHTML = `${sign}${weeklyAverageStress}%`;
        }
    }

    // 스트레스 섹션의 '아직 기록된 퇴사지수가 없어요' 메시지는 숨김 처리
    // (퇴사지수가 없어도 그래프는 표시해야 함)
    const stressNoDataElement = document.querySelector('.stress-section .stress-no-data');
    if (stressNoDataElement) {
        stressNoDataElement.style.display = 'none';
    }

    // 모달 관련 코드
    const modal = document.getElementById('timeSettingsModal');
    const settingsBtn = document.querySelector('.time-settings-btn');
    const closeBtn = document.querySelector('.close-modal-btn');
    const saveBtn = document.querySelector('.save-time-btn');

    if (settingsBtn) {
        settingsBtn.addEventListener('click', () => {
            if (modal) {
                // 현재 설정된 시간 값들을 input에 설정
                document.getElementById('workStartTime').value = globalStartTime;
                document.getElementById('lunchTime').value = globalLunchTime;
                document.getElementById('workEndTime').value = globalEndTime;
                modal.style.display = 'flex';
            }
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            if (modal) modal.style.display = 'none';
        });
    }

    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.style.display = 'none';
        });
    }

    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            const workStartInput = document.getElementById('workStartTime');
            const lunchInput = document.getElementById('lunchTime');
            const workEndInput = document.getElementById('workEndTime');

            if (!workStartInput || !lunchInput || !workEndInput) {
                console.error('Some time inputs are missing');
                return;
            }

            const newStartTime = workStartInput.value;
            const newLunchTime = lunchInput.value;
            const newEndTime = workEndInput.value;

            if (!newStartTime || !newLunchTime || !newEndTime) {
                alert('모든 시간을 입력해주세요.');
                return;
            }

            // 전역 변수 업데이트
            globalStartTime = newStartTime;
            globalLunchTime = newLunchTime;
            globalEndTime = newEndTime;

            // 즉시 화면 업데이트
            updateAllProgress();

            // 모달 닫기
            modal.style.display = 'none';
        });
    }

    // 진행 상태 업데이트 시작
    updateAllProgress();
    setInterval(updateAllProgress, 60000); // 1분마다 업데이트
});