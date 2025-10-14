// import { AiOutlineDelete, AiOutlineUpload } from 'react-icons/ai';
// import { confirmAlert } from 'react-confirm-alert';
// import api from '@/utils/axiosInstance.js';
// import { toast } from 'react-toastify';
//

// import { toast } from 'react-toastify';
// import api from '@/utils/axiosInstance.js';

import { useRef, useState } from 'react';
import { useMapSearch } from '@/recoil/fetchAppState.js';

// const fileInputRef = useRef(null);
// const [deleteText, setDeleteText] = useState('');
// const mapSearch = useMapSearch();
//
// const handleExcelUploadClick = () => fileInputRef.current?.click();
//
// const handleExcelFileChange = async e => {
//   const file = e.target.files[0];
//   if (!file) return;
//   const name = file.name.toLowerCase();
//   if (!name.endsWith('.xlsx') && !name.endsWith('.xls')) {
//     toast.error('엑셀 파일(.xlsx, .xls)만 업로드할 수 있습니다.');
//     return;
//   }
//   const formData = new FormData();
//   formData.append('multipartFile', file);
//   try {
//     await api.post('/cham/upload', formData, {
//       headers: { 'Content-Type': 'multipart/form-data' },
//     });
//     toast.success('엑셀 파일이 업로드되었습니다.');
//     await handleSearch();
//   } catch (err) {
//     console.error(err);
//     toast.error(err.response?.data?.message ?? '업로드 실패');
//   } finally {
//     e.target.value = '';
//   }
// };

// const handleSearch = () => {
//   const params = {
//     cardOwnerPositionId: searchCondition.selectedRole?.value,
//     input: searchCondition.input,
//     sortOrder: searchCondition.sortOrder,
//     addrDetail: '',
//   };
//   mapSearch(params);
// };

// <ExcelSection>
//   <ExcelButton onClick={handleExcelUploadClick}>
//     <AiOutlineUpload /> 추가
//   </ExcelButton>
//   <input
//     type="file"
//     ref={fileInputRef}
//     accept=".xlsx,.xls"
//     style={{ display: 'none' }}
//     onChange={handleExcelFileChange}
//   />
//   <DeleteInput
//     value={deleteText}
//     type="text"
//     onChange={e => setDeleteText(e.target.value)}
//     placeholder="삭제키를 입력해주세요."
//   />
//   <ExcelButton
//     onClick={() => {
//       confirmAlert({
//         message: '해당 엑셀을 삭제하시겠습니까?',
//         buttons: [
//           {
//             label: '삭제',
//             onClick: async () => {
//               try {
//                 await api.delete(`/cham/upload/${deleteText}`);
//                 toast.success('엑셀 삭제가 완료되었습니다.');
//                 await handleSearch();
//               } catch (e) {
//                 console.error(e);
//                 const msg = e.response?.data?.message ?? '삭제 실패';
//                 toast.error(msg);
//               }
//             },
//           },
//           { label: '취소', onClick: () => {} },
//         ],
//       });
//     }}
//   >
//     <AiOutlineDelete /> 삭제
//   </ExcelButton>
// </ExcelSection>

// import styled from 'styled-components';
//
// const ExcelSection = styled.div`
//   position: absolute;
//   top: 10px;
//   left: 50%;
//   transform: translate(-50%, 0);
//   display: flex;
//   background-color: #ffffff;
//   border-radius: 999px;
//   border: 2px solid ${({ theme }) => theme.colors.primary};
//   justify-content: center;
//   align-items: center;
//   padding: 4px;
//   gap: 8px;
//   z-index: 2;
// `;
// const ExcelButton = styled.button`
//   display: flex;
//   align-items: center;
//   background: ${({ color, theme }) => color || theme.colors.primary};
//   border: none;
//   color: white;
//   font-weight: bold;
//   font-size: ${({ theme }) => theme.sizes.medium};
//   padding: 10px 16px;
//   border-radius: 999px;
//   cursor: pointer;
//   white-space: nowrap;
//   gap: 6px;
//   svg {
//     width: 20px;
//     height: 20px;
//   }
//   @media ${({ theme }) => theme.device.mobile} {
//     font-size: ${({ theme }) => theme.sizes.small};
//     padding: 6px 12px;
//   }
// `;
// const DeleteInput = styled.input`
//   border: none;
//   background: unset;
//   padding: 4px;
//   &:focus {
//     outline: none;
//     border-bottom: 2px solid ${({ theme }) => theme.colors.primary};
//   }
// `;
