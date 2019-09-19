import React from 'react';
import ReactDOM from 'react-dom';

function App() {
    const [state, setState] = React.useState(null);
    const [currentMower, setCurrentMower] = React.useState(null);
    const [numGrassCut, setNumGrassCut] = React.useState(0);
    const [numCraters, setnumCraters] = React.useState(0);

    React.useEffect(() => {
        handleAction('state');
    }, []);

    if (!state || !state.lawn)
        return null;

    function getSquareIcon(square) {
        let squareIconSrc = '';
        switch (square.type) {
            case 'Grass':
                squareIconSrc = '//localhost:8080/images/grass.png';
                break;
            case 'Crater':
                squareIconSrc = '//localhost:8080/images/crater.png';
                break;
            case 'Empty':
                squareIconSrc = '//localhost:8080/images/empty.png';
                break;
            case 'MowerOccupied':
                squareIconSrc = `//localhost:8080/images/mower_${square.direction.toLowerCase()}.png`;
                break;
            case 'Crash':
                squareIconSrc = `//localhost:8080/images/crash.png`;
                break;
            case 'ChargingStation':
                squareIconSrc = `//localhost:8080/images/charging_pad.png`;
                break;
            default:
                break;
        }
        return squareIconSrc;
    }

    function handleAction(type) {
        let endpoint = '//localhost:8080/api/state';
        switch (type) {
            case 'state':
                endpoint = '//localhost:8080/api/state';
                break;
            case 'next':
                endpoint = '//localhost:8080/api/next';
                break;
            case 'fastforward':
                endpoint = '//localhost:8080/api/fastforward';
                break;
            case 'stop':
                endpoint = '//localhost:8080/api/stop';
                break;
            default:
                throw new Error('Action type not implemented');
                break;
        }

        fetch(endpoint).then(res => {
            res.json().then(data => {
                data.mowers.forEach(mower => {
                    let mowerOccupied = data.lawn.lawnState[mower.currentPosition.x][mower.currentPosition.y];
                    mowerOccupied.type = "MowerOccupied";
                    mowerOccupied.direction = mower.currentDirection;
                });
                setState(data);
                countGrassCut(data.lawn.lawnState);
                setCurrentMower(data.mowers[data.currentMowerIndex]);
            });
        });
    }

    function countGrassCut(lawnState) {
        let count = 0;
        let craters = 0;

        lawnState.forEach(row => {
           row.forEach(square => {
               if(square.type === "Empty")
                   count++;
               if(square.type === "Crater")
                   craters++;
           }) ;
        });
        setNumGrassCut(count);
        setnumCraters(craters);
        return count;
    }
    
    function getDoneMessage() {
        switch (state.completeCode.toLowerCase()) {
            case 'no-grass':
                return "Done! All Grass has Been Cut!";
            case 'no-turns-left':
                return "Number of turns exceeded. Simulation Ended.";
            case 'mowers-done':
                return "Simulation Complete. Mowers cannot move any longer.";
            default: return '';
        }
    }

    function getNextMower() {
        let currentIndex = state.currentMowerIndex;

        if (currentIndex >= state.mowers.length) {
            currentIndex = 0;
        }

        return state.mowers[currentIndex].id.substring(0,8);
    }

    return (
        <div className="app-wrapper">
            <div className="lawn-wrapper">
                <div className="lawn-container">
                    {state.lawn.lawnState.map((row, rIndex) => (
                        <div className="lawn-row" key={rIndex}>
                            {row.map((square, sIndex) => (
                                <div className="lawn-square" key={sIndex}>
                                    <img src={getSquareIcon(square)} alt={square.type} width={60} height={60}/>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            </div>
            {state.isSimulationComplete && <div className="done-message">{getDoneMessage()}</div>}
            <div className="information-wrapper">

                {currentMower && <div className="current-mower">
                    <p className="title">Current Mower</p>
                    <p className="text">{currentMower.id.substring(0, 8)}</p>

                    <p className="title">Action</p>
                    {state.lastAction ?
                        <p className="text">{state.lastAction.action}, {state.lastAction.distance}, {state.lastAction.direction}</p> :
                        <p className="text">No Action Taken Yet</p>
                    }

                    <p className="title">Next Mower</p>
                    <p className="text">{getNextMower()}</p>
                    <br/>
                </div>}

                <div className="sim-summary">
                    <p className="title">Summary</p>
                    <p className="detail">Total Size of the Lawn: {state.lawn.height * state.lawn.width}</p>
                    <p className="detail">Number of Grass Cut: {numGrassCut}</p>
                    <p className="detail">Number of Grass Remaining: {state.originalNumOfGrass - numGrassCut - numCraters}</p>
                    <p className="detail">Number of Turns taken: {state.turnsTaken === 0 ? 'No Turns Taken' : state.turnsTaken}</p>
                    <p className="detail">Number of Turns Left: {state.maxNumTurns - state.turnsTaken}</p>
                </div>

                <div className="app-actions">
                    <div className="actions-wrapper">
                        <p className="title">Control Panel</p>
                        <button disabled={state.isSimulationComplete} onClick={() => handleAction('next')}>Next</button>
                        <button disabled={state.isSimulationComplete} onClick={() => handleAction('fastforward')}>Fast Forward</button>
                        <button onClick={() => handleAction('stop')}>Stop</button>
                    </div>
                </div>
            </div>

            <div className="mowers-wrapper">
                {state.mowers.map((mower, mIndex) => (
                    <div className="mower-item" key={mIndex}>
                        <p className="mower-id">
                            <img src="//localhost:8080/images/mower.png" alt=""/>
                            {mower.id.substring(0, 8)}
                        </p>
                        <p>
                            <img src="//localhost:8080/images/energy.png" alt=""/>
                            Energy: {mower.indEnergyRem}
                        </p>
                        {!mower.hasCrashed && <p>
                            {mower.indEnergyRem > 0 ? <img src="//localhost:8080/images/active.png" alt=""/> :
                            <img src="//localhost:8080/images/stall.png" alt=""/>}
                            {mower.done ? "Done" : mower.indEnergyRem === 0 ? "Energy Drained" : "Active"}
                        </p>}
                        {mower.hasCrashed && <p>
                            <img src="//localhost:8080/images/stall.png" alt=""/>
                            Stalled
                        </p>}
                        <p>
                            <img src="//localhost:8080/images/location.png" alt=""/>
                            {mower.currentPosition.x}, {mower.currentPosition.y}, {mower.currentDirection}
                        </p>
                    </div>
                ))}
            </div>
        </div>
    )
}

ReactDOM.render(
    <App/>,
    document.getElementById('root')
);