import styled from 'styled-components';
import { useState, useEffect } from 'react';
import 'react-datepicker/dist/react-datepicker.css';
import { FiSearch, FiRotateCcw } from 'react-icons/fi';
import Select from 'react-select';
import { useSelectSearchState } from '@/recoil/useAppState.js';
import { useMapSearch } from '@/recoil/fetchAppState.js';
import { useRecoilState, useRecoilValue } from 'recoil';
import { mapCenterAddrState, mapSearchFilterState } from '@/recoil/appState.js';

export default function SearchBar() {
  const mapSearch = useMapSearch();

  const [searchCondition, setSearchCondition] = useRecoilState(mapSearchFilterState);

  const centerAddr = useRecoilValue(mapCenterAddrState);

  const handleSearch = () => {
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      input: searchCondition.input,
      sortOrder: searchCondition.sortOrder,
      addrDetail: '',
    };

    mapSearch(params);
  };

  //초기화
  const resetSearch = () => {
    setSearchCondition({
      cardOwnerPositionId: null,
      input: '',
      sortOrder: 1,
      sortValue: { value: 1, label: '최신순' },
      selectedRole: { label: '전체', value: null },
    });

    mapSearch();
  };

  //직위
  const { selectData, selectLoading } = useSelectSearchState();

  const [roleOptions, setRoleOptions] = useState([]);

  //정렬
  const sortOptions = [
    { value: 1, label: '최신순' },
    { value: 2, label: '오래된순' },
    { value: 3, label: '가격순' },
    { value: 4, label: '방문횟수순' },
  ];

  const searchSelect = {
    control: provided => ({
      ...provided,
      width: 100,
      height: 28,
      minHeight: 20,
      border: 'none',
      cursor: 'pointer',
      fontSize: '13px',
      boxShadow: 'none',
      outline: 'none',
      '&:hover': {
        borderColor: '#093A6E',
        outline: 'none',
      },
    }),
    singleValue: provided => ({
      ...provided,
      color: 'black',
      fontSize: '13px',
    }),
    option: (provided, state) => ({
      ...provided,
      fontSize: '13px',
      color: state.isSelected ? '#fff' : '#093A6E',
      backgroundColor: state.isSelected ? '#093A6E' : '#fff',
      cursor: 'pointer',
    }),
    indicatorsContainer: provided => ({
      ...provided,
      display: 'none',
    }),
    menuPortal: base => ({ ...base, zIndex: 3 }),
    menu: provided => ({
      ...provided,
      width: 110,
      zIndex: 9999,
    }),
  };

  const customSelectStyles = {
    control: (provided, state) => ({
      ...provided,
      width: 110,
      height: 30,
      borderColor: '#093A6E',
      borderWidth: 2,
      cursor: 'pointer',
      fontSize: '12px',
      color: '#093A6E',
      boxShadow: 'none',
      outline: 'none',
      '&:hover': {
        borderColor: '#093A6E',
        outline: 'none',
      },
    }),
    singleValue: provided => ({
      ...provided,
      color: '#093A6E',
      fontWeight: 'bold',
      fontSize: '12px',
    }),
    option: (provided, state) => ({
      ...provided,
      fontSize: '12px',
      color: state.isSelected ? '#fff' : '#093A6E',
      backgroundColor: state.isSelected ? '#093A6E' : '#fff',
      cursor: 'pointer',
    }),
    menu: provided => ({
      ...provided,
      width: 110,
      zIndex: 9999,
    }),
  };

  useEffect(() => {
    if (!selectLoading && selectData.length > 0) {
      const defaultOption = { label: '전체', value: null };
      const optionsWithAll = [defaultOption, ...selectData];
      setRoleOptions(optionsWithAll);

      setSearchCondition(prev => ({
        ...prev,
        selectedRole: prev.selectedRole ?? defaultOption,
      }));
    }
  }, [selectLoading, selectData]);

  useEffect(() => {
    handleSearch();
  }, []);

  return (
    <Wrapper>
      <SearchGroup
        onSubmit={e => {
          e.preventDefault();
          handleSearch();
        }}
      >
        <FieldsWrapper>
          <FieldSelect>
            <label>직위</label>
            <SortSelect>
              <Select
                value={searchCondition.selectedRole}
                options={roleOptions}
                onChange={option => setSearchCondition(prev => ({ ...prev, selectedRole: option }))}
                styles={searchSelect}
                isSearchable={false}
                menuPortalTarget={document.body}
              />
            </SortSelect>
          </FieldSelect>

          <Divider />

          <Field>
            <label>지역, 사용자, 이름, 집행목적</label>
            <input
              type="text"
              value={searchCondition.input}
              onChange={e =>
                setSearchCondition(prev => ({
                  ...prev,
                  input: e.target.value,
                }))
              }
            />
          </Field>
        </FieldsWrapper>

        <SearchButton type="submit">
          <SearchIcon size={22} />
          검색
        </SearchButton>
        <ResetButton type="button" onClick={() => resetSearch()}>
          <ResetIcon size={22} />
          초기화
        </ResetButton>
      </SearchGroup>

      <BottomRow>
        <CountText>
          검색결과 <strong>{centerAddr ? Object.keys(centerAddr).length : 0} 건</strong>
        </CountText>
        <SortSelect>
          <Select
            options={sortOptions}
            value={searchCondition.sortValue}
            styles={customSelectStyles}
            isSearchable={false}
            onChange={option => {
              setSearchCondition(prev => ({
                ...prev,
                sortOrder: option.value,
                sortValue: option,
              }));
            }}
          />
        </SortSelect>
      </BottomRow>
    </Wrapper>
  );
}

const Wrapper = styled.section`
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
`;

const SearchGroup = styled.form`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 4px 4px 20px;
  border: 2px solid ${({ theme }) => theme.colors.primary};
  border-radius: 999px;
  flex-wrap: nowrap;
`;

const FieldsWrapper = styled.div`
  display: flex;
  align-items: center;
  width: 100%;
  gap: 20px;
  flex: 1;
  overflow-x: auto;
  flex-wrap: nowrap;
  overflow-y: hidden;
`;

const FieldSelect = styled.div`
  display: flex;
  flex-direction: column;
  width: 100px;
  flex-shrink: 0;

  label {
    font-size: ${({ theme }) => theme.sizes.small};
    color: ${({ theme }) => theme.colors.liteGray};
    font-weight: bold;
    text-align: left;
    margin-left: 6px;
  }
`;

const Field = styled.div`
  display: flex;
  flex-direction: column;
  flex: 1;

  label {
    font-size: ${({ theme }) => theme.sizes.small};
    color: ${({ theme }) => theme.colors.liteGray};
    font-weight: bold;
    text-align: left;
    margin-left: 6px;
  }

  input {
    border: none;
    padding: 6px 4px;
    font-size: ${({ theme }) => theme.sizes.medium};
    outline: none;
    width: 100%;
    color: black;
    background: transparent;
    cursor: pointer;
  }
`;

const Divider = styled.div`
  width: 1px;
  height: 36px;
  background: #ddd;
  flex-shrink: 0;
`;

const SearchButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ theme }) => theme.colors.primary};
  border: none;
  color: white;
  font-weight: bold;
  padding: 12px 24px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
`;

const ResetButton = styled.button`
  display: flex;
  align-items: center;
  margin-left: 4px;
  background: ${({ theme }) => theme.colors.liteGray};
  border: none;
  color: white;
  font-weight: bold;
  padding: 12px 24px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
`;

const SearchIcon = styled(FiSearch)`
  margin-right: 6px;
`;

const ResetIcon = styled(FiRotateCcw)`
  margin-right: 6px;
`;

const BottomRow = styled.div`
  display: flex;
  margin-top: 10px;
  align-items: center;
  justify-content: space-between;
`;

const CountText = styled.span`
  white-space: nowrap;
  font-size: ${({ theme }) => theme.colors.medium};
  font-weight: bold;
  color: ${({ theme }) => theme.colors.liteGray};
  strong {
    color: ${({ theme }) => theme.colors.primary};
    font-weight: bold;
  }
`;

const SortSelect = styled.div`
  display: flex;
`;
