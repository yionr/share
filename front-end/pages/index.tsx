import React from 'react';

import { test } from '@/pages/api/share'
const App: React.FC = () => {

    async function handleClick() {
        let result = await test()
        alert(result.data)
    }


    return (
        <button onClick={handleClick} > he</button>
    );
}

export default App;