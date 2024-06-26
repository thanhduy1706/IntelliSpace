import type { Metadata } from 'next';
import TopLoader from '@/components/TopLoader';
import NavBar from '@/components/NavBar';
import Footer from '@/components/Footer';

export const metadata: Metadata = {
  title: 'Reset password | IntelliSpace',
  description: 'Reset your password.',
};

const RegisterLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <>
      <TopLoader />
      <NavBar />
      {children}
      <Footer />
    </>
  );
};

export default RegisterLayout;
