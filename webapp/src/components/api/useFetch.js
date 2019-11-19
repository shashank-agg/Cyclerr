import { useState, useEffect } from "react";

export default function UseFetch(url) {

    const [data, setData]= useState({});
    const [hasError, setErrors] = useState(false);
    useEffect(() => {
      fetch(url)
            .then(res => res.json())
            .then(res => {console.log(res); setData(res)})
            .catch(err => setErrors(err))
    }, [url])

    return data;
}
