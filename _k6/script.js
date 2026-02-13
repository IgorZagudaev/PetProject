import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 10 },  // 10 пользователей за 30 сек
        { duration: '1m', target: 50 },   // увеличиваем до 50 за 1 минуту
        { duration: '20s', target: 0 },   // плавное завершение
    ],
};

const BASE_URL = 'http://localhost:8081/api/v1/accounts';

export default function () {
    // Уникальный идентификатор для каждого виртуального пользователя
    const userId = __VU; // Встроенная переменная k6

    const headers = {
        'Content-Type': 'application/json',
        'X-User-ID': userId.toString(), // Преобразуем в строку
    };

    // Логируем для отладки
    //console.log(`User ${userId} making request...`);

    // GET запрос
    let getResponse = http.get(`${BASE_URL}`, { headers });
    // Логируем статус и ошибки
    /*if (getResponse.status !== 200) {
        console.log(`❌ Status: ${getResponse.status}`);
        console.log(`❌ Error: ${getResponse.error}`);
        console.log(`❌ Body: ${getResponse.body.substring(0, 200)}`);
    }*/
    // проверяем статус и время ответа
    check(getResponse, {
        'GET status is 200': (r) => r.status === 200,
        'GET response time < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);

    // POST запрос
/*        name: 'Test Item',
        description: 'Load test item'
    });

    const headers = { 'Content-Type': 'application/json' };
    let postResponse = http.post(`${BASE_URL}/items`, payload, { headers });
    check(postResponse, {
        'POST status is 201': (r) => r.status === 201,
        'POST response time < 300ms': (r) => r.timings.duration < 300,
    });

    sleep(1);*/
}