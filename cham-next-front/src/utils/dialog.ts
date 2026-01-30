import { useDialog } from '@/app/components/DialogProvider';

export const useDialogUtil = () => {
  const { alert, confirm } = useDialog();

  return {
    alert: (title: string, description?: string) => alert(title, description),

    confirm: async (title: string, description?: string) => await confirm(title, description),
  };
};
