let mouth;

let g = 9.8

let refreshRateMs = 17
let updateRate = 30

let lr_v = 0;
let ud_v = 0;

let actual_gamma;
let actual_beta;

let xPosEntity;
let yPosEntity;

let rEntity;

let rMouth = 50;

let xPosMouth;
let yPosMouth;

let entity;
let entityExists = false;

let caught = 0;
let points = 0;
let pointsElem;

let lifes = 3;
let lifesElem;

let timeLeft = 10999;
let timeLeftElem;

let mainLoopHandler;

let chewing = false;
let chewTimestamp = 0;

function start() {
    document.getElementById('startMessage').style.display = 'none';
    mouth = document.getElementById('ball');

    pointsElem = document.getElementById('points')
    pointsElem.innerText = points.toString();

    lifesElem = document.getElementById('lifes')
    lifesElem.innerText = lifes.toString();

    timeLeftElem = document.getElementById('timeLeft')
    timeLeftElem.innerText = Math.floor(timeLeft / 1000).toString();

    intro();
}

function intro() {
    document.getElementById('introMessage').style.display = 'flex';
}

function startMainLoop() {
    let checkTime = undefined;
    let open = true;
    let openChanged = undefined;
    mainLoopHandler = setInterval(() => {
        if (!checkTime || Date.now() - checkTime > updateRate) {
            checkTime = Date.now();
            window.addEventListener('deviceorientationabsolute', event => {
                actual_gamma = event.gamma;
                actual_beta = event.beta;
            }, {once: true});
        }

        if (chewing) {
            if (Date.now() - chewTimestamp > 1000) {
                chewing = false;
                mouth.style.backgroundImage = "url('../img/mouth.png')";
                open = true;
            }

            if (!openChanged || Date.now() - openChanged > 100) {
                openChanged = Date.now();
                if (open) {
                    mouth.style.backgroundImage = "url('../img/closed.png')";
                    open = false;
                } else {
                    mouth.style.backgroundImage = "url('../img/mouth.png')";
                    open = true;
                }
            }
        }

        timeLeft -= refreshRateMs;

        if (timeLeft <= 3000) {
            timeLeftElem.innerText = (timeLeft / 1000).toFixed(1);
        } else {
            timeLeftElem.innerText = Math.floor(timeLeft / 1000).toString();
        }

        checkTimeLeft();
        checkPos();
        calcSpeed();
        move();
        generateAndEntityCollision();
    }, refreshRateMs)
}

function checkTimeLeft() {
    if (timeLeft <= 0) {
        clearInterval(mainLoopHandler);
        timeLeft = 0;
        timeLeftElem.innerText = timeLeft;
        document.getElementById('retryTimeMessage').style.display = 'flex';
        if (lifes === 0) {
            document.getElementById('lostMessage').style.display = 'flex';
        } else {
            lifes--;
            timeLeft = 10999;
            lifesElem.innerText = lifes;
            document.getElementById('retryTimeMessage').style.display = 'flex';
        }
    }
}

function retry() {
    document.getElementById('retryMessage').style.display = 'none';
    document.getElementById('retryTimeMessage').style.display = 'none';
    resetMouth();
    timeLeft = 10999;
    startMainLoop();
}

function restart() {
    document.getElementById('lostMessage').style.display = 'none';
    document.getElementById('introMessage').style.display = 'none';

    resetMouth();
    removeEntity();
    lifes = 3;
    lifesElem.innerText = lifes;
    timeLeft = 10999;
    timeLeftElem.innerText = Math.floor(timeLeft / 1000);

    Array.prototype.slice.call(document.getElementsByTagName('crumbs')).forEach(item => {
        item.remove();
    });

    startMainLoop();
}

function checkPos() {
    if (xPosMouth > (window.innerWidth - rMouth) || xPosMouth < 0
        || yPosMouth > (window.innerHeight - rMouth) || yPosMouth < 0.1 * window.innerHeight) {
        clearInterval(mainLoopHandler);

        if (lifes === 0) {
            document.getElementById('lostMessage').style.display = 'flex';
        } else {
            lifes--;
            lifesElem.innerText = lifes;
            document.getElementById('retryMessage').style.display = 'flex';
        }
    }
}

function generateRandomPos() {
    return [
        Math.floor(Math.random() * (0.8 * window.innerWidth) + (0.05 * window.innerWidth)),
        Math.floor(Math.random() * (0.7 * window.innerHeight) + (0.1 * window.innerHeight))
    ]
}

function chew() {
    chewing = true;
    chewTimestamp = Date.now();
}

function generateAndEntityCollision() {
    if (!entityExists) {
        entity = document.getElementById('entity');
        pos = generateRandomPos();

        xPosEntity = pos[0];
        yPosEntity = pos[1];

        rEntity = ((1 / (caught + 1) + 5) / 6) * 90;

        entity.style.left = xPosEntity.toString() + 'px';
        entity.style.top = yPosEntity.toString() + 'px';

        entity.style.width = rEntity + 'px';
        entity.style.height = rEntity + 'px';

        entity.style.display = 'flex';
        entityExists = true;
    }

    let distX = (xPosMouth + rMouth / 2) - (xPosEntity + rEntity / 2);
    let distY = (yPosMouth + rMouth / 2) - (yPosEntity + rEntity / 2);



    if (Math.sqrt((distX * distX) + (distY * distY)) < (rEntity + rMouth) / 2) {
        removeEntity();
        chew();
        caught++;
        points += (0.2 * caught) * 100;
        timeLeft = 10000;
        pointsElem.innerText = points.toString();

    }
}

function removeEntity() {
    if (entity) {
        let crumbsElem = document.createElement('crumbs');
        crumbsElem.style.left = (xPosEntity + (rEntity / 2)).toString() + 'px';
        crumbsElem.style.top = (yPosEntity + (rEntity / 2)).toString() + 'px';
        crumbsElem.style.transform = 'rotate(' + (Math.random() * 180).toString() + 'deg)';
        crumbsElem.style.opacity = Math.random().toString();

        document.getElementsByTagName('body').item(0).appendChild(crumbsElem);

        entity.style.display = 'none';
        entityExists = false;
    }
}

function calcSpeed() {
    actual_gamma = actual_gamma === undefined ? 0 : actual_gamma;
    actual_beta = actual_beta === undefined ? 0 : actual_beta;

    lr_v += Math.sin(actual_gamma * Math.PI / 180) * g * (refreshRateMs / 1000);
    ud_v += Math.sin(actual_beta * Math.PI / 180) * g * (refreshRateMs / 1000);
}

function move() {
    movePixels(lr_v, ud_v)

}

function movePixels(dx, dy) {
    xPosMouth += dx;
    mouth.style.left = xPosMouth + 'px';

    yPosMouth += dy;
    mouth.style.top = yPosMouth + 'px';
}

function resetMouth() {
    lr_v = 0;
    ud_v = 0;

    chewing = false;
    mouth.style.backgroundImage = "url('../img/mouth.png')";

    xPosMouth = (window.innerWidth) / 2;
    mouth.style.left = xPosMouth + 'px';

    yPosMouth = (window.innerHeight) / 2;
    mouth.style.top = yPosMouth + 'px';
}
