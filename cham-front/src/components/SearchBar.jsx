import styled from 'styled-components';
import { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { FiSearch } from 'react-icons/fi';
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
    const rawAmount = searchCondition.numberOfVisits?.replace(/,/g, '');
    const params = {
      cardOwnerPositionId: searchCondition.selectedRole?.value,
      cardUseName: searchCondition.cardUseName,
      numberOfVisits: parseInt(rawAmount, 10),
      startDate: searchCondition.startDate?.toISOString().split('T')[0],
      endDate: searchCondition.endDate?.toISOString().split('T')[0],
      sortOrder: searchCondition.sortOrder,
      addrDetail: '',
    };

    mapSearch(params);
  };

  //직위
  const { selectData, selectLoading } = useSelectSearchState();

  const [roleOptions, setRoleOptions] = useState([]);

  //정렬
  const sortOptions = [
    { value: 1, label: '최신순' },
    { value: 2, label: '오래된순' },
  ];

  const searchSelect = {
    control: provided => ({
      ...provided,
      width: 100,
      height: 30,
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
      <SearchGroup>
        <FieldsWrapper>
          <Field>
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
          </Field>

          <Divider />

          <Field>
            <label>이름</label>
            <input
              type="text"
              value={searchCondition.cardUseName}
              onChange={e =>
                setSearchCondition(prev => ({
                  ...prev,
                  cardUseName: e.target.value,
                }))
              }
            />
          </Field>

          <Divider />

          <Field>
            <label>방문횟수</label>
            <input
              type="text"
              value={searchCondition.numberOfVisits}
              onChange={e => {
                const raw = e.target.value.replace(/[^0-9]/g, '');
                const formatted = raw.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                setSearchCondition(prev => ({
                  ...prev,
                  numberOfVisits: formatted,
                }));
              }}
            />
          </Field>

          <Divider />

          <Field>
            <label>집행일자</label>
            <DateRange>
              <DatePicker
                selected={searchCondition.startDate}
                onChange={date =>
                  setSearchCondition(prev => ({
                    ...prev,
                    startDate: date,
                  }))
                }
                dateFormat="yyyy.MM.dd"
                portalId="root-portal"
              />
              <DateCenter>-</DateCenter>
              <DatePicker
                selected={searchCondition.endDate}
                onChange={date =>
                  setSearchCondition(prev => ({
                    ...prev,
                    endDate: date,
                  }))
                }
                dateFormat="yyyy.MM.dd"
                portalId="root-portal"
              />
            </DateRange>
          </Field>
        </FieldsWrapper>

        <SearchButton onClick={handleSearch}>
          <SearchIcon size={22} />
          검색
        </SearchButton>
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

const SearchGroup = styled.div`
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
  gap: 20px;
  flex: 1;
  overflow-x: auto;
  flex-wrap: nowrap;
`;

const Field = styled.div`
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

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
    width: 80px;
    color: black;
    background: transparent;
    cursor: pointer;
    @media ${({ theme }) => theme.device.mobile} {
      width: 80px;
    }
  }
`;

const DateCenter = styled.div`
  display: flex;
  margin-right: 12px;
`;

const DateRange = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;

  span {
    font-size: 14px;
    font-weight: bold;
    color: ${({ theme }) => theme.colors.liteGray};
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

const SearchIcon = styled(FiSearch)`
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
