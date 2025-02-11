import { useState } from 'react';
import type { ChangeEvent } from 'react';

const useInput = (initialValue = '') => {
  const [value, setValue] = useState(initialValue);

  const handleValueChange = (event: ChangeEvent<HTMLInputElement>) => {
    setValue(event.target.value);
  };

  return {
    value,
    onValueChange: handleValueChange,
  };
};

export default useInput;
