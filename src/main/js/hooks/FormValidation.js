import React, {useEffect, useState} from "react";

const useFormValidation = (initialState, validate, submitHandler) => {
    const [values, setValues] = useState(initialState);
    const [errors, setErrors] = useState({});
    const [isSubmitting, setSubmitting] = useState(false);

    useEffect( async () => {
        if (isSubmitting) {
            const noErrors = Object.keys(errors).length === 0;
            if (noErrors) {
                await submitHandler(values);
                setSubmitting(false);
            } else {
                setSubmitting(false);
            }
        }
    }, [errors, isSubmitting]);

    const handleChange = (event) => {
        setValues({
            ...values,
            [event.target.name]: event.target.value
        });
    }

    const handleInvertedChange = (event) => {
        setValues({
            ...values,
            [event.target.name]: !event.target.value
        });
    }

    const handleCheckboxChange = (event) => {
        setValues({
            ...values,
            [event.target.name]: event.target.checked
        });
    }

    const handleBlur = () => {
        const validationErrors = validate(values);
        setErrors(validationErrors);
    }

    const handleSubmit = (event) => {
        event.preventDefault();
        const validationErrors = validate(values);
        setErrors(validationErrors);
        setSubmitting(true);
    }

    return {
        handleSubmit,
        handleChange,
        handleCheckboxChange,
        handleInvertedChange,
        handleBlur,
        values,
        errors,
        isSubmitting
    };
}

export default useFormValidation;